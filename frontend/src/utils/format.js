/**
 * Author: HoangTLT
 */
export const money = (value) => new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' }).format(value);
export const bookingStatus = { CONFIRMED: 'ÄÃ£ xÃ¡c nháº­n', COMPLETED: 'ÄÃ£ hoÃ n táº¥t', CANCELLED: 'ÄÃ£ há»§y' };

// Local calendar date; avoid UTC date shifts near midnight in Vietnam.
export function today() {
  const date = new Date();
  return `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}-${String(date.getDate()).padStart(2, '0')}`;
}

export function nights(start, end) {
  return Math.round((Date.parse(end) - Date.parse(start)) / 86400000);
}
