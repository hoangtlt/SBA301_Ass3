param(
    [string]$SqlHost = 'localhost',
    [int]$Port = 1433,
    [ValidatePattern('^[A-Za-z][A-Za-z0-9_]*$')]
    [string]$Database = 'A3StudentName_ClassCode'
)

# Optional Windows shortcut. Uses your Windows account, so no SQL password is needed.
# SQL Server must already be running and allow this Windows account to create/use the database.
$ErrorActionPreference = 'Stop'
$sqlcmdCommand = Get-Command sqlcmd.exe -ErrorAction SilentlyContinue
$sqlcmdPath = if ($sqlcmdCommand) { $sqlcmdCommand.Source } else {
    'C:/Program Files/Microsoft SQL Server/Client SDK/ODBC/170/Tools/Binn/SQLCMD.EXE'
}
if (-not (Test-Path -LiteralPath $sqlcmdPath)) {
    throw 'sqlcmd.exe not found. Use the SQL login instructions in README.md instead.'
}

& $sqlcmdPath -S "tcp:$SqlHost,$Port" -E -b -l 10 -Q "IF DB_ID(N'$Database') IS NULL CREATE DATABASE [$Database];"
if ($LASTEXITCODE -ne 0) { throw 'Cannot create/open the assignment database using Windows Authentication.' }

# Native authentication library matching the JDBC version managed by Spring Boot 3.5.16.
$nativeDirectory = Join-Path $PSScriptRoot '.tools/sqlserver-auth'
$nativeDll = Join-Path $nativeDirectory 'mssql-jdbc_auth-12.10.2.x64.dll'
New-Item -ItemType Directory -Force -Path $nativeDirectory | Out-Null
if (-not (Test-Path -LiteralPath $nativeDll)) {
    & curl.exe --fail --location --max-time 60 --silent --show-error `
        'https://repo.maven.apache.org/maven2/com/microsoft/sqlserver/mssql-jdbc_auth/12.10.2.x64/mssql-jdbc_auth-12.10.2.x64.dll' `
        --output $nativeDll
    if ($LASTEXITCODE -ne 0) {
        if (Test-Path -LiteralPath $nativeDll) { Remove-Item -LiteralPath $nativeDll }
        throw 'Cannot download the Microsoft JDBC Windows authentication library.'
    }
}

Push-Location (Join-Path $PSScriptRoot 'backend')
try {
    & .\mvnw.cmd -q -DskipTests clean package
    if ($LASTEXITCODE -ne 0) { throw 'Backend build failed. Stop any running copy of the backend JAR first.' }
    # artifactId can change when you rename the project; use the generated executable JAR.
    $jar = Get-ChildItem -LiteralPath target -Filter '*.jar' | Select-Object -First 1
    $jdbcUrl = "jdbc:sqlserver://${SqlHost}:$Port;databaseName=$Database;encrypt=true;trustServerCertificate=true;integratedSecurity=true"
    & java "-Djava.library.path=$nativeDirectory" -jar $jar.FullName `
        --spring.profiles.active=sqlserver "--spring.datasource.url=$jdbcUrl" --debug=false
    if ($LASTEXITCODE -ne 0) { throw 'Backend stopped with an error.' }
}
finally { Pop-Location }
