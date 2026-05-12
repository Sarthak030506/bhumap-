// Seed data for LandLedger UI kit.
window.LL_DATA = {
  currentAdmin: { name: "Sarthak Joshi", role: "admin", company: "Green Valley Developers" },
  currentBuyer: { name: "Rohan Patil", role: "buyer" },
  projects: [
    { id: "p1", name: "Green Valley Phase 1", location: "Nashik, MH", plots: 48, sold: 34, reserved: 6, available: 8, collected: 42_80_500, overdue: 1_87_500 },
    { id: "p2", name: "Sahyadri Heights", location: "Pune, MH", plots: 24, sold: 11, reserved: 4, available: 9, collected: 18_50_000, overdue: 0 },
    { id: "p3", name: "Riverside Layout", location: "Wai, MH", plots: 16, sold: 5, reserved: 2, available: 9, collected: 6_00_000, overdue: 25_000 },
  ],
  plots: [
    { id: "A14", num: "A-14", area: 2400, price: 18_00_000, status: "available", project: "p1", buyer: null },
    { id: "A15", num: "A-15", area: 2400, price: 18_00_000, status: "sold", project: "p1", buyer: "Rohan Patil" },
    { id: "B02", num: "B-02", area: 1800, price: 13_50_000, status: "reserved", project: "p1", buyer: "Meera Shetty" },
    { id: "B03", num: "B-03", area: 1800, price: 13_50_000, status: "available", project: "p1", buyer: null },
    { id: "C07", num: "C-07", area: 2100, price: 15_75_000, status: "sold", project: "p1", buyer: "Rohan Patil" },
    { id: "C08", num: "C-08", area: 2100, price: 15_75_000, status: "sold", project: "p1", buyer: "Amit Kale" },
    { id: "D01", num: "D-01", area: 3000, price: 22_50_000, status: "hold", project: "p1", buyer: null },
    { id: "D02", num: "D-02", area: 3000, price: 22_50_000, status: "available", project: "p1", buyer: null },
    { id: "D03", num: "D-03", area: 2800, price: 21_00_000, status: "sold", project: "p1", buyer: "Priya Desai" },
  ],
  buyerPlots: [
    { plotId: "C07", num: "C-07", area: 2100, total: 15_75_000, paid: 4_50_000, due: 25_000, project: "Green Valley Phase 1" },
    { plotId: "A15", num: "A-15", area: 2400, total: 18_00_000, paid: 12_00_000, due: 0, project: "Green Valley Phase 1" },
  ],
  emiSchedule: [
    { n: 1, date: "10 Jan 2026", amount: 12500, status: "paid", paidOn: "09 Jan", mode: "UPI" },
    { n: 2, date: "10 Feb 2026", amount: 12500, status: "paid", paidOn: "10 Feb", mode: "UPI" },
    { n: 3, date: "10 Mar 2026", amount: 12500, status: "paid", paidOn: "08 Mar", mode: "UPI" },
    { n: 4, date: "10 Apr 2026", amount: 12500, status: "paid", paidOn: "11 Apr", mode: "Cash" },
    { n: 5, date: "10 May 2026", amount: 12500, status: "due",  paidOn: null, mode: null, daysLeft: 3 },
    { n: 6, date: "10 Jun 2026", amount: 12500, status: "overdue", paidOn: null, mode: null, daysLate: 12 },
    { n: 7, date: "10 Jul 2026", amount: 12500, status: "upcoming", paidOn: null, mode: null },
    { n: 8, date: "10 Aug 2026", amount: 12500, status: "upcoming", paidOn: null, mode: null },
  ],
  transactions: [
    { id: "LL-2026-00418", date: "11 Apr 2026", amount: 12500, mode: "Cash", plot: "C-07", buyer: "Rohan Patil" },
    { id: "LL-2026-00402", date: "08 Mar 2026", amount: 12500, mode: "UPI",  plot: "C-07", buyer: "Rohan Patil" },
    { id: "LL-2026-00381", date: "10 Feb 2026", amount: 12500, mode: "UPI",  plot: "C-07", buyer: "Rohan Patil" },
    { id: "LL-2026-00350", date: "09 Jan 2026", amount: 12500, mode: "UPI",  plot: "C-07", buyer: "Rohan Patil" },
    { id: "LL-2026-00312", date: "22 Dec 2025", amount: 4_00_000, mode: "Bank transfer", plot: "C-07", buyer: "Rohan Patil" },
  ],
};

// Indian digit grouping: ₹1,50,000
window.formatINR = function(n, withSymbol = true) {
  if (n == null) return "—";
  const s = Math.round(n).toString();
  const last3 = s.slice(-3);
  const rest = s.slice(0, -3);
  const grouped = rest ? rest.replace(/\B(?=(\d{2})+(?!\d))/g, ",") + "," + last3 : last3;
  return withSymbol ? "₹" + grouped : grouped;
};
