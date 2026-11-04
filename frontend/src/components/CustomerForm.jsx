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
      <label>Họ và tên<input name="customerFullName" value={form.customerFullName} onChange={change} required maxLength={100} /></label>
      <label>Số điện thoại<input name="telephone" type="tel" value={form.telephone} onChange={change} required minLength={8} maxLength={20} /></label>
      <label>Email<input name="emailAddress" type="email" value={form.emailAddress} onChange={change} required maxLength={100} /></label>
      <label>Ngày sinh<input name="customerBirthday" type="date" value={form.customerBirthday} onChange={change} required /></label>
      <label>{editing ? 'Mật khẩu mới (để trống nếu giữ nguyên)' : 'Mật khẩu'}
        <input name="password" type="password" value={form.password} onChange={change} required={!editing} minLength={8} maxLength={72} autoComplete="new-password" />
      </label>
      {staff && <label>Trạng thái<select name="customerStatus" value={form.customerStatus} onChange={change}>
        <option value={1}>Hoạt động</option><option value={0}>Ngừng hoạt động</option>
      </select></label>}
      <div className="actions full"><button disabled={busy}>{busy ? 'Đang lưu...' : 'Lưu thông tin'}</button>
        {onCancel && <button type="button" className="secondary" onClick={onCancel}>Đóng</button>}
      </div>
    </form>
  );
}
