// Buyer screens — Home, My plot, EMI schedule, Payment history
/* global React, Icon, Button, StatusChip, Card, Amount, AppBar, Scroll, ListRow, formatINR, LL_DATA */

// ---------- Buyer Home ----------
const BuyerHome = ({ onOpenPlot, onEMI }) => {
  const plot = LL_DATA.buyerPlots[0];
  const nextDue = LL_DATA.emiSchedule.find(e => e.status === "due" || e.status === "overdue");
  return (
    <>
      <div style={{ background: "var(--ll-soil-900)", color: "var(--ll-fg-on-dark)",
                    padding: "16px 16px 28px", position: "relative", overflow: "hidden" }}>
        <div style={{ position: "absolute", inset: 0, backgroundImage: "url(../../assets/pattern-contour.svg)", filter: "invert(1) brightness(1.4)", opacity: 0.25 }} />
        <div style={{ position: "relative", display: "flex", justifyContent: "space-between", alignItems: "center" }}>
          <div>
            <div className="ll-hindi" style={{ fontSize: 13, opacity: 0.7, fontWeight: 500 }}>नमस्ते,</div>
            <div style={{ fontSize: 22, fontWeight: 700, letterSpacing: "-0.01em" }}>Rohan Patil</div>
          </div>
          <div style={{ width: 40, height: 40, borderRadius: 999, background: "var(--ll-terracotta-500)", display: "flex", alignItems: "center", justifyContent: "center", fontWeight: 700, fontSize: 15 }}>RP</div>
        </div>
        <div style={{ position: "relative", marginTop: 18, fontSize: 12, opacity: 0.7, fontWeight: 600, letterSpacing: ".06em", textTransform: "uppercase" }}>Next payment due</div>
        <div style={{ position: "relative", marginTop: 4, display: "flex", justifyContent: "space-between", alignItems: "flex-end" }}>
          <Amount value={nextDue.amount} size="xxl" color="#fff" />
          <div style={{ textAlign: "right", fontSize: 12, opacity: 0.8 }}>
            <div>{nextDue.date}</div>
            <div style={{ color: "var(--ll-terracotta-300)", fontWeight: 600, marginTop: 2 }}>{nextDue.status === "overdue" ? `Overdue ${nextDue.daysLate} days` : `In ${nextDue.daysLeft} days`}</div>
          </div>
        </div>
        <div style={{ position: "relative", marginTop: 14 }}>
          <Button fullWidth size="lg" variant="primary">Pay {formatINR(nextDue.amount)}</Button>
        </div>
      </div>

      <Scroll style={{ marginTop: -16, borderTopLeftRadius: 20, borderTopRightRadius: 20, paddingTop: 20 }}>
        <div style={{ fontSize: 11, fontWeight: 600, letterSpacing: ".06em", textTransform: "uppercase", color: "var(--ll-fg-muted)", marginBottom: 10 }}>Your plots</div>
        {LL_DATA.buyerPlots.map(p => (
          <Card key={p.plotId} style={{ padding: 16, marginBottom: 10 }} onClick={() => onOpenPlot(p)}>
            <div style={{ display: "flex", justifyContent: "space-between", alignItems: "flex-start" }}>
              <div>
                <div style={{ fontSize: 11, fontWeight: 600, color: "var(--ll-fg-muted)" }}>{p.project}</div>
                <div style={{ fontWeight: 700, fontSize: 18, marginTop: 2 }}>Plot {p.num}</div>
                <div style={{ fontSize: 12, color: "var(--ll-fg-muted)", marginTop: 2 }}>{p.area.toLocaleString("en-IN")} sq.ft</div>
              </div>
              <Amount value={p.total} size="md" />
            </div>
            <div style={{ height: 5, borderRadius: 3, background: "var(--ll-paper-200)", marginTop: 14, overflow: "hidden" }}>
              <div style={{ width: `${(p.paid / p.total) * 100}%`, height: "100%", background: "var(--ll-primary)" }} />
            </div>
            <div style={{ display: "flex", justifyContent: "space-between", fontSize: 11.5, marginTop: 8, color: "var(--ll-fg-muted)", fontWeight: 600 }}>
              <span>Paid <b style={{ color: "var(--ll-fg)" }}>{formatINR(p.paid)}</b></span>
              <span>{Math.round((p.paid/p.total)*100)}% complete</span>
            </div>
          </Card>
        ))}

        <div style={{ fontSize: 11, fontWeight: 600, letterSpacing: ".06em", textTransform: "uppercase", color: "var(--ll-fg-muted)", marginTop: 18, marginBottom: 8 }}>Shortcuts</div>
        <Card padded={false} style={{ padding: "4px 16px" }}>
          <ListRow icon="calendar" title="EMI schedule" subtitle="24 installments · 4 paid" onClick={onEMI} />
          <ListRow icon="doc"      title="Payment receipts" subtitle="5 documents available" onClick={() => {}} />
          <ListRow icon="phone"    title="Contact developer" subtitle="Green Valley Developers" onClick={() => {}} />
        </Card>
      </Scroll>
    </>
  );
};

// ---------- Buyer EMI schedule (full) ----------
const BuyerEMISchedule = ({ onBack }) => {
  const paid    = LL_DATA.emiSchedule.filter(e => e.status === "paid").reduce((s, e) => s + e.amount, 0);
  const due     = LL_DATA.emiSchedule.filter(e => e.status === "due").reduce((s, e) => s + e.amount, 0);
  const overdue = LL_DATA.emiSchedule.filter(e => e.status === "overdue").reduce((s, e) => s + e.amount, 0);
  return (
    <>
      <AppBar title="EMI schedule" subtitle="Plot C-07" onBack={onBack}
        right={<button style={{ width:36,height:36,borderRadius:10,border:"none",background:"var(--ll-paper-100)",display:"flex",alignItems:"center",justifyContent:"center" }}><Icon name="download" size={18} color="var(--ll-fg)" /></button>} />
      <Scroll>
        <div style={{ display: "grid", gridTemplateColumns: "repeat(3,1fr)", gap: 8, marginBottom: 14 }}>
          <Card style={{ padding: 10 }}><div style={{ fontSize: 10, fontWeight: 600, color: "var(--ll-fg-muted)", letterSpacing: ".04em", textTransform: "uppercase" }}>Paid</div><Amount value={paid} size="sm" color="var(--ll-status-paid)" /></Card>
          <Card style={{ padding: 10 }}><div style={{ fontSize: 10, fontWeight: 600, color: "var(--ll-fg-muted)", letterSpacing: ".04em", textTransform: "uppercase" }}>Due</div><Amount value={due} size="sm" color="var(--ll-status-due)" /></Card>
          <Card style={{ padding: 10 }}><div style={{ fontSize: 10, fontWeight: 600, color: "var(--ll-fg-muted)", letterSpacing: ".04em", textTransform: "uppercase" }}>Overdue</div><Amount value={overdue} size="sm" color="var(--ll-status-overdue)" /></Card>
        </div>

        <Card padded={false} style={{ padding: 0, overflow: "hidden" }}>
          <div style={{ display: "grid", gridTemplateColumns: "44px 1fr 90px 80px", gap: 8, padding: "10px 14px", background: "var(--ll-paper-100)", borderBottom: "1px solid var(--ll-border)", fontSize: 10, fontWeight: 700, letterSpacing: ".06em", textTransform: "uppercase", color: "var(--ll-fg-muted)" }}>
            <span>#</span><span>Due</span><span style={{ textAlign: "right" }}>Amount</span><span style={{ textAlign: "right" }}>Status</span>
          </div>
          {LL_DATA.emiSchedule.map((e, i) => (
            <div key={e.n} style={{ display: "grid", gridTemplateColumns: "44px 1fr 90px 80px", gap: 8, padding: "12px 14px", borderBottom: i < LL_DATA.emiSchedule.length - 1 ? "1px solid var(--ll-border)" : "none", alignItems: "center" }}>
              <span style={{ fontFamily: "var(--ll-font-mono)", fontWeight: 700, fontSize: 13 }}>{String(e.n).padStart(2,"0")}</span>
              <div>
                <div style={{ fontSize: 13, fontWeight: 500 }}>{e.date}</div>
                <div style={{ fontSize: 11, color: "var(--ll-fg-muted)", marginTop: 2 }}>
                  {e.status === "paid" ? `Paid ${e.paidOn} · ${e.mode}` :
                   e.status === "due" ? `In ${e.daysLeft} days` :
                   e.status === "overdue" ? `Overdue ${e.daysLate} days` : "Upcoming"}
                </div>
              </div>
              <span style={{ textAlign: "right", fontFamily: "var(--ll-font-mono)", fontWeight: 600, fontSize: 13, fontVariantNumeric: "tabular-nums" }}>{formatINR(e.amount)}</span>
              <span style={{ justifySelf: "end" }}><StatusChip status={e.status} shape="sq" /></span>
            </div>
          ))}
        </Card>
      </Scroll>
    </>
  );
};

// ---------- Buyer Payment history (transactions) ----------
const BuyerHistory = ({ onBack }) => (
  <>
    <AppBar title="Payment history" onBack={onBack} />
    <Scroll>
      <Card padded={false} style={{ padding: "4px 16px" }}>
        {LL_DATA.transactions.map(t => (
          <ListRow key={t.id}
            icon={t.mode === "UPI" ? "upi" : t.mode === "Cash" ? "cash" : t.mode === "Cheque" ? "cheque" : "bank"}
            title={formatINR(t.amount)}
            subtitle={`${t.date} · ${t.mode} · Plot ${t.plot}`}
            rightChip={<span style={{ fontFamily: "var(--ll-font-mono)", fontSize: 10, color: "var(--ll-fg-subtle)" }}>{t.id}</span>}
          />
        ))}
      </Card>
      <div style={{ textAlign: "center", marginTop: 14, fontSize: 11.5, color: "var(--ll-fg-subtle)" }}>End of history · 5 transactions</div>
    </Scroll>
  </>
);

Object.assign(window, { BuyerHome, BuyerEMISchedule, BuyerHistory });
