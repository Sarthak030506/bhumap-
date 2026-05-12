// Admin screens — Home, Project detail, Plot detail, Record payment
/* global React, Icon, Button, Field, StatusChip, Card, Amount, Sheet, ListRow, AppBar, Scroll, formatINR, LL_DATA */
const { useState: useStateA } = React;

// ---------- Admin Home (dashboard) ----------
const AdminHome = ({ onOpenProject, onRecordPayment }) => {
  const totalCollected = LL_DATA.projects.reduce((s, p) => s + p.collected, 0);
  const totalOverdue = LL_DATA.projects.reduce((s, p) => s + p.overdue, 0);
  return (
    <>
      <AppBar
        title="Dashboard"
        subtitle={LL_DATA.currentAdmin.company}
        right={
          <button style={{ width: 36, height: 36, borderRadius: 10, border: "none", background: "var(--ll-paper-100)", display: "flex", alignItems: "center", justifyContent: "center" }}>
            <Icon name="bell" size={18} color="var(--ll-fg)" />
          </button>
        }
      />
      <Scroll>
        {/* Headline stats */}
        <div style={{ display: "grid", gridTemplateColumns: "1fr 1fr", gap: 10, marginBottom: 16 }}>
          <Card style={{ padding: 14 }}>
            <div style={{ fontSize: 11, fontWeight: 600, letterSpacing: ".06em", textTransform: "uppercase", color: "var(--ll-fg-muted)" }}>Collected</div>
            <Amount value={totalCollected} size="lg" />
            <div style={{ fontSize: 11, color: "var(--ll-status-available-fg)", marginTop: 4, fontWeight: 600 }}>▲ {formatINR(125000)} this month</div>
          </Card>
          <Card style={{ padding: 14 }}>
            <div style={{ fontSize: 11, fontWeight: 600, letterSpacing: ".06em", textTransform: "uppercase", color: "var(--ll-fg-muted)" }}>Overdue</div>
            <Amount value={totalOverdue} size="lg" color="var(--ll-status-overdue)" />
            <div style={{ fontSize: 11, color: "var(--ll-fg-muted)", marginTop: 4 }}>9 installments · 5 buyers</div>
          </Card>
        </div>

        {/* Quick action */}
        <Card style={{ padding: 14, marginBottom: 16, background: "var(--ll-soil-900)", color: "var(--ll-fg-on-dark)", border: "none" }}>
          <div style={{ fontSize: 11, fontWeight: 600, letterSpacing: ".06em", textTransform: "uppercase", opacity: 0.7 }}>Quick actions</div>
          <div style={{ display: "flex", gap: 8, marginTop: 10 }}>
            <Button variant="primary" size="sm" icon="plus" onClick={onRecordPayment}>Record payment</Button>
            <Button size="sm" style={{ background: "rgba(255,255,255,.08)", color: "#fff" }} icon="doc">Add plot</Button>
          </div>
        </Card>

        {/* Projects */}
        <div style={{ display: "flex", alignItems: "center", justifyContent: "space-between", marginBottom: 10 }}>
          <div style={{ fontSize: 11, fontWeight: 600, letterSpacing: ".06em", textTransform: "uppercase", color: "var(--ll-fg-muted)" }}>Projects</div>
          <span style={{ fontSize: 12, color: "var(--ll-primary)", fontWeight: 600 }}>See all</span>
        </div>
        <div style={{ display: "flex", flexDirection: "column", gap: 10 }}>
          {LL_DATA.projects.map(p => (
            <Card key={p.id} onClick={() => onOpenProject(p)} padded={false} style={{ padding: 14 }}>
              <div style={{ display: "flex", justifyContent: "space-between", alignItems: "flex-start", gap: 12 }}>
                <div style={{ flex: 1, minWidth: 0 }}>
                  <div style={{ fontWeight: 600, fontSize: 15, color: "var(--ll-fg)" }}>{p.name}</div>
                  <div style={{ fontSize: 12, color: "var(--ll-fg-muted)", marginTop: 2, display: "flex", alignItems: "center", gap: 4 }}>
                    <Icon name="pin" size={12} />{p.location}
                  </div>
                </div>
                <Icon name="chevR" size={16} color="var(--ll-fg-subtle)" />
              </div>
              <div style={{ display: "flex", gap: 6, marginTop: 10, fontSize: 11, fontWeight: 600 }}>
                <span style={{ padding: "3px 8px", borderRadius: 6, background: "var(--ll-status-available-bg)", color: "var(--ll-status-available-fg)" }}>● {p.available} available</span>
                <span style={{ padding: "3px 8px", borderRadius: 6, background: "var(--ll-status-reserved-bg)", color: "var(--ll-status-reserved-fg)" }}>● {p.reserved} reserved</span>
                <span style={{ padding: "3px 8px", borderRadius: 6, background: "var(--ll-status-sold-bg)", color: "var(--ll-status-sold-fg)" }}>● {p.sold} sold</span>
              </div>
              <div style={{ height: 1, background: "var(--ll-border)", margin: "12px 0" }} />
              <div style={{ display: "flex", justifyContent: "space-between", alignItems: "baseline" }}>
                <span style={{ fontSize: 11, color: "var(--ll-fg-muted)", fontWeight: 600 }}>Collected</span>
                <Amount value={p.collected} size="sm" />
              </div>
            </Card>
          ))}
        </div>
      </Scroll>
    </>
  );
};

// ---------- Project detail with plot map ----------
const AdminProjectDetail = ({ project, onBack, onOpenPlot }) => {
  const plots = LL_DATA.plots.filter(p => p.project === project.id);
  const statusColor = { available: "#1F8A5B", reserved: "#C68A18", sold: "#B23A3A", hold: "#4A5568" };
  const plotPositions = [
    { id: "A14", x: 20, y: 40, w: 60, h: 50 }, { id: "A15", x: 82, y: 40, w: 60, h: 50 },
    { id: "B02", x: 20, y: 94, w: 50, h: 50 }, { id: "B03", x: 72, y: 94, w: 50, h: 50 },
    { id: "C07", x: 20, y: 148, w: 55, h: 50 }, { id: "C08", x: 77, y: 148, w: 55, h: 50 },
    { id: "D01", x: 20, y: 202, w: 70, h: 52 }, { id: "D02", x: 92, y: 202, w: 70, h: 52 }, { id: "D03", x: 164, y: 202, w: 62, h: 52 },
  ];
  return (
    <>
      <AppBar title={project.name} subtitle={project.location} onBack={onBack}
        right={<button style={{ width:36,height:36,borderRadius:10,border:"none",background:"var(--ll-paper-100)",display:"flex",alignItems:"center",justifyContent:"center" }}><Icon name="more" size={18} color="var(--ll-fg)" /></button>} />
      <Scroll>
        <Card style={{ padding: 0, overflow: "hidden", marginBottom: 14 }}>
          <div style={{ position: "relative", height: 270, background: "var(--ll-paper-100)", backgroundImage: "url(../../assets/pattern-contour.svg)" }}>
            <svg viewBox="0 0 360 280" style={{ width: "100%", height: "100%", display: "block" }}>
              {/* Road */}
              <rect x="0" y="84" width="360" height="6" fill="var(--ll-soil-300)" opacity="0.5"/>
              <rect x="146" y="0" width="6" height="280" fill="var(--ll-soil-300)" opacity="0.5"/>
              {plotPositions.map(pos => {
                const plot = plots.find(p => p.id === pos.id);
                if (!plot) return null;
                return (
                  <g key={pos.id} onClick={() => onOpenPlot(plot)} style={{ cursor: "pointer" }}>
                    <rect x={pos.x} y={pos.y} width={pos.w} height={pos.h}
                          fill={statusColor[plot.status]} fillOpacity="0.18"
                          stroke={statusColor[plot.status]} strokeWidth="1.5" />
                    <text x={pos.x + pos.w/2} y={pos.y + pos.h/2 + 4} textAnchor="middle"
                          fontFamily="JetBrains Mono, monospace" fontSize="11" fontWeight="700"
                          fill={statusColor[plot.status]}>{plot.num}</text>
                  </g>
                );
              })}
            </svg>
          </div>
          <div style={{ padding: 12, display: "flex", gap: 6, flexWrap: "wrap", borderTop: "1px solid var(--ll-border)" }}>
            <StatusChip status="available">{project.available} available</StatusChip>
            <StatusChip status="reserved">{project.reserved} reserved</StatusChip>
            <StatusChip status="sold">{project.sold} sold</StatusChip>
          </div>
        </Card>

        <div style={{ fontSize: 11, fontWeight: 600, letterSpacing: ".06em", textTransform: "uppercase", color: "var(--ll-fg-muted)", marginBottom: 8 }}>All plots</div>
        <Card padded={false} style={{ padding: "4px 16px" }}>
          {plots.map(p => (
            <ListRow key={p.id}
              title={`Plot ${p.num}`}
              subtitle={`${p.area.toLocaleString("en-IN")} sq.ft · ${p.buyer || "—"}`}
              right={formatINR(p.price)}
              rightChip={<StatusChip status={p.status} />}
              onClick={() => onOpenPlot(p)} />
          ))}
        </Card>
      </Scroll>
    </>
  );
};

// ---------- Plot detail ----------
const AdminPlotDetail = ({ plot, onBack, onRecordPayment }) => (
  <>
    <AppBar title={`Plot ${plot.num}`} subtitle="Green Valley Phase 1" onBack={onBack} />
    <Scroll>
      {/* Hero */}
      <Card style={{ padding: 16, marginBottom: 14 }}>
        <div style={{ display: "flex", justifyContent: "space-between", alignItems: "flex-start" }}>
          <div>
            <div style={{ fontSize: 11, fontWeight: 600, letterSpacing: ".06em", textTransform: "uppercase", color: "var(--ll-fg-muted)" }}>Sale price</div>
            <Amount value={plot.price} size="xl" />
          </div>
          <StatusChip status={plot.status} />
        </div>
        <div style={{ height: 1, background: "var(--ll-border)", margin: "14px 0" }} />
        <div style={{ display: "grid", gridTemplateColumns: "1fr 1fr", gap: 14 }}>
          <div><div style={{ fontSize: 11, color: "var(--ll-fg-muted)", fontWeight: 600 }}>Area</div><div style={{ fontWeight: 600 }}>{plot.area.toLocaleString("en-IN")} sq.ft</div></div>
          <div><div style={{ fontSize: 11, color: "var(--ll-fg-muted)", fontWeight: 600 }}>Buyer</div><div style={{ fontWeight: 600 }}>{plot.buyer || "—"}</div></div>
          <div><div style={{ fontSize: 11, color: "var(--ll-fg-muted)", fontWeight: 600 }}>Plan</div><div style={{ fontWeight: 600 }}>EMI · 24 months</div></div>
          <div><div style={{ fontSize: 11, color: "var(--ll-fg-muted)", fontWeight: 600 }}>Monthly</div><div style={{ fontWeight: 600, fontFamily: "var(--ll-font-mono)" }}>{formatINR(12500)}</div></div>
        </div>
      </Card>

      {/* Progress */}
      <Card style={{ padding: 16, marginBottom: 14 }}>
        <div style={{ display: "flex", justifyContent: "space-between", marginBottom: 8 }}>
          <span style={{ fontSize: 11, fontWeight: 600, letterSpacing: ".06em", textTransform: "uppercase", color: "var(--ll-fg-muted)" }}>Payment progress</span>
          <span style={{ fontWeight: 600, fontSize: 12 }}>4 of 24</span>
        </div>
        <div style={{ height: 6, borderRadius: 3, background: "var(--ll-paper-200)", overflow: "hidden" }}>
          <div style={{ width: "17%", height: "100%", background: "var(--ll-primary)", borderRadius: 3 }} />
        </div>
        <div style={{ display: "flex", justifyContent: "space-between", marginTop: 8, fontSize: 12 }}>
          <span style={{ color: "var(--ll-fg-muted)" }}>Collected <b style={{ color: "var(--ll-fg)" }}>{formatINR(450000)}</b></span>
          <span style={{ color: "var(--ll-fg-muted)" }}>Remaining <b style={{ color: "var(--ll-fg)" }}>{formatINR(1125000)}</b></span>
        </div>
      </Card>

      {/* EMI table preview */}
      <div style={{ fontSize: 11, fontWeight: 600, letterSpacing: ".06em", textTransform: "uppercase", color: "var(--ll-fg-muted)", marginBottom: 8 }}>Upcoming installments</div>
      <Card padded={false} style={{ padding: "4px 16px", marginBottom: 14 }}>
        {LL_DATA.emiSchedule.slice(3, 7).map(e => (
          <ListRow key={e.n}
            title={`#${String(e.n).padStart(2,"0")} · ${e.date}`}
            subtitle={e.status === "paid" ? `Paid ${e.paidOn} · ${e.mode}` : e.status === "due" ? `Due in ${e.daysLeft} days` : e.status === "overdue" ? `Overdue ${e.daysLate} days` : "Upcoming"}
            right={formatINR(e.amount)}
            rightChip={<StatusChip status={e.status} shape="sq" />} />
        ))}
      </Card>

      <Button fullWidth variant="primary" size="lg" icon="plus" onClick={onRecordPayment}>Record payment</Button>
    </Scroll>
  </>
);

// ---------- Record payment sheet ----------
const RecordPaymentSheet = ({ open, onClose, onDone }) => {
  const [amount, setAmount] = useStateA("12,500");
  const [mode, setMode] = useStateA("UPI");
  return (
    <Sheet open={open} onClose={onClose} title="Record payment">
      <div style={{ display: "flex", flexDirection: "column", gap: 14 }}>
        <div style={{ padding: 12, background: "var(--ll-paper-100)", borderRadius: 10, display: "flex", justifyContent: "space-between", alignItems: "center" }}>
          <div>
            <div style={{ fontSize: 11, color: "var(--ll-fg-muted)", fontWeight: 600 }}>Plot C-07 · Rohan Patil</div>
            <div style={{ fontWeight: 600, marginTop: 2 }}>Installment #05 · 10 May 2026</div>
          </div>
          <StatusChip status="due" shape="sq">Due in 3 days</StatusChip>
        </div>
        <Field label="Amount received" prefix="₹" value={amount} onChange={setAmount} hint="Indian digit grouping applied automatically" />
        <div>
          <div style={{ fontSize: 12, fontWeight: 600, color: "var(--ll-fg-muted)", marginBottom: 6 }}>Payment mode</div>
          <div style={{ display: "grid", gridTemplateColumns: "1fr 1fr", gap: 8 }}>
            {[["UPI","upi"],["Cash","cash"],["Bank transfer","bank"],["Cheque","cheque"]].map(([m, ic]) => (
              <div key={m} onClick={() => setMode(m)} style={{
                padding: "12px 10px", borderRadius: 10,
                border: `1px solid ${mode === m ? "var(--ll-primary)" : "var(--ll-border-strong)"}`,
                background: mode === m ? "var(--ll-primary-soft)" : "var(--ll-surface)",
                color: mode === m ? "var(--ll-primary)" : "var(--ll-fg)",
                fontWeight: 600, fontSize: 13, display: "flex", alignItems: "center", gap: 8, cursor: "pointer",
              }}><Icon name={ic} size={16} /> {m}</div>
            ))}
          </div>
        </div>
        <Button fullWidth variant="primary" size="lg" onClick={onDone}>Confirm ₹{amount} payment</Button>
        <div style={{ fontSize: 11, color: "var(--ll-fg-subtle)", textAlign: "center" }}>A receipt will be generated and shared with the buyer.</div>
      </div>
    </Sheet>
  );
};

Object.assign(window, { AdminHome, AdminProjectDetail, AdminPlotDetail, RecordPaymentSheet });
