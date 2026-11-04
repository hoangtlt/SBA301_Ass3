/**
 * Author: HoangTLT
 */
export const emptyCustomer = {
  customerFullName: '', telephone: '', emailAddress: '', customerBirthday: '', customerStatus: 1, password: '',
};

// Shared by registration, profile and Staff's customer form.
export default function CustomerForm({ form, setForm, onSubmit, editing = false, staff = false, busy = false, onCancel }) {
  function change(event) {
    const { name, value } = event.target;
    setForm({ ...form, [name]: name === 'customerStatus' ? Number(value) : value });
  }

  return (
    <form onSubmit={onSubmit} className="form-grid">
      <label>Há» vÃ  tÃªn<input name="customerFullName" value={form.customerFullName} onChange={change} required maxLength={100} /></label>
      <label>Sá»‘ Ä‘iá»‡n thoáº¡i<input name="telephone" type="tel" value={form.telephone} onChange={change} required minLength={8} maxLength={20} /></label>
      <label>Email<input name="emailAddress" type="email" value={form.emailAddress} onChange={change} required maxLength={100} /></label>
      <label>NgÃ y sinh<input name="customerBirthday" type="date" value={form.customerBirthday} onChange={change} required /></label>
      <label>{editing ? 'Máº­t kháº©u má»›i (Ä‘á»ƒ trá»‘ng náº¿u giá»¯ nguyÃªn)' : 'Máº­t kháº©u'}
        <input name="password" type="password" value={form.password} onChange={change} required={!editing} minLength={8} maxLength={72} autoComplete="new-password" />
      </label>
      {staff && <label>Tráº¡ng thÃ¡i<select name="customerStatus" value={form.customerStatus} onChange={change}>
        <option value={1}>Hoáº¡t Ä‘á»™ng</option><option value={0}>Ngá»«ng hoáº¡t Ä‘á»™ng</option>
      </select></label>}
      <div className="actions full"><button disabled={busy}>{busy ? 'Äang lÆ°u...' : 'LÆ°u thÃ´ng tin'}</button>
        {onCancel && <button type="button" className="secondary" onClick={onCancel}>ÄÃ³ng</button>}
      </div>
    </form>
  );
}
