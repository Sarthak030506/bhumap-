// LandLedger primitives — shared components, icons, tokens.
/* global React */
const { useState, useEffect, useRef } = React;

// ============================================================
// Icons — Lucide-style, 24x24, 1.75px stroke
// ============================================================
const Icon = ({ name, size = 20, color = "currentColor", strokeWidth = 1.75, style }) => {
  const paths = {
    home:       <><path d="M3 12l9-8 9 8"/><path d="M5 10v10h14V10"/></>,
    list:       <><path d="M3 6h18"/><path d="M3 12h18"/><path d="M3 18h18"/></>,
    wallet:     <><rect x="3" y="5" width="18" height="14" rx="2"/><path d="M3 10h18"/></>,
    user:       <><circle cx="12" cy="8" r="4"/><path d="M4 21c1-4 5-6 8-6s7 2 8 6"/></>,
    search:     <><circle cx="11" cy="11" r="7"/><path d="M21 21l-4-4"/></>,
    pin:        <><path d="M12 2a7 7 0 017 7c0 5-7 13-7 13S5 14 5 9a7 7 0 017-7z"/><circle cx="12" cy="9" r="2.5"/></>,
    calendar:   <><rect x="3" y="5" width="18" height="16" rx="2"/><path d="M3 9h18"/><path d="M8 3v4"/><path d="M16 3v4"/></>,
    clock:      <><circle cx="12" cy="12" r="8"/><path d="M12 8v4l3 2"/></>,
    check:      <><path d="M5 12l4 4 10-10"/></>,
    close:      <><path d="M6 6l12 12"/><path d="M18 6L6 18"/></>,
    plus:       <><path d="M12 5v14"/><path d="M5 12h14"/></>,
    filter:     <><path d="M4 4h16v4l-6 6v6l-4-2v-4L4 8z"/></>,
    doc:        <><rect x="4" y="3" width="16" height="18" rx="2"/><path d="M8 8h8M8 12h8M8 16h5"/></>,
    download:   <><path d="M4 12l8 8 8-8"/><path d="M12 4v16"/></>,
    back:       <><path d="M15 18l-6-6 6-6"/></>,
    fwd:        <><path d="M9 6l6 6-6 6"/></>,
    more:       <><circle cx="12" cy="5" r="1.5"/><circle cx="12" cy="12" r="1.5"/><circle cx="12" cy="19" r="1.5"/></>,
    bell:       <><path d="M18 16V11a6 6 0 10-12 0v5l-2 3h16z"/><path d="M10 21h4"/></>,
    lock:       <><rect x="5" y="11" width="14" height="10" rx="2"/><path d="M8 11V8a4 4 0 018 0v3"/></>,
    map:        <><path d="M3 6l6-2 6 2 6-2v14l-6 2-6-2-6 2z"/><path d="M9 4v16"/><path d="M15 6v16"/></>,
    report:     <><rect x="4" y="4" width="16" height="16" rx="2"/><path d="M8 14v2M12 10v6M16 12v4"/></>,
    phone:      <><path d="M5 4h4l2 5-3 2a11 11 0 006 6l2-3 5 2v4a2 2 0 01-2 2A16 16 0 013 6a2 2 0 012-2z"/></>,
    rupee:      <><path d="M6 4h12M6 8h12M6 8c4 0 8 0 8 4s-4 4-8 4"/><path d="M6 12l9 8"/></>,
    ring:       <><circle cx="12" cy="12" r="9"/><path d="M12 8v5l3 2"/></>,
    chevR:      <><path d="M9 6l6 6-6 6"/></>,
    upi:        <><path d="M4 20L20 4"/><path d="M4 4l6 16L14 10l6-6"/></>,
    cash:       <><rect x="2" y="7" width="20" height="11" rx="2"/><circle cx="12" cy="12.5" r="2.5"/></>,
    bank:       <><path d="M3 10L12 4l9 6"/><path d="M5 10v9M9 10v9M15 10v9M19 10v9"/><path d="M3 21h18"/></>,
    cheque:     <><rect x="3" y="6" width="18" height="12" rx="1"/><path d="M7 10h10M7 14h6"/></>,
    sparkle:    <><path d="M12 3l2 6 6 2-6 2-2 6-2-6-6-2 6-2z"/></>,
    star:       <><path d="M12 3l3 6 6 1-4.5 4 1 6L12 17l-5.5 3 1-6L3 10l6-1z"/></>,
  };
  return (
    <svg width={size} height={size} viewBox="0 0 24 24" fill="none"
         stroke={color} strokeWidth={strokeWidth} strokeLinecap="round" strokeLinejoin="round"
         style={{ flexShrink: 0, ...style }}>
      {paths[name] || null}
    </svg>
  );
};

// ============================================================
// Button
// ============================================================
const Button = ({ children, variant = "primary", size = "md", icon, iconRight, fullWidth, onClick, disabled, style }) => {
  const base = {
    fontFamily: "inherit", fontWeight: 600, border: "none", cursor: disabled ? "not-allowed" : "pointer",
    display: "inline-flex", alignItems: "center", justifyContent: "center", gap: 8,
    transition: "background 90ms, transform 90ms, opacity 90ms",
    borderRadius: 10, width: fullWidth ? "100%" : "auto", opacity: disabled ? 0.4 : 1,
  };
  const sizes = {
    sm: { padding: "8px 12px", fontSize: 13, borderRadius: 8 },
    md: { padding: "12px 16px", fontSize: 14 },
    lg: { padding: "14px 20px", fontSize: 15 },
  };
  const variants = {
    primary:   { background: "var(--ll-primary)", color: "#fff" },
    secondary: { background: "var(--ll-surface)", color: "var(--ll-fg)", border: "1px solid var(--ll-border-strong)" },
    ghost:     { background: "transparent", color: "var(--ll-primary)" },
    danger:    { background: "var(--ll-status-sold)", color: "#fff" },
    dark:      { background: "var(--ll-soil-900)", color: "#fff" },
  };
  return (
    <button onClick={disabled ? undefined : onClick} style={{ ...base, ...sizes[size], ...variants[variant], ...style }}>
      {icon && <Icon name={icon} size={size === "sm" ? 16 : 18} />}
      {children}
      {iconRight && <Icon name={iconRight} size={size === "sm" ? 16 : 18} />}
    </button>
  );
};

// ============================================================
// Field
// ============================================================
const Field = ({ label, value, onChange, placeholder, prefix, hint, type = "text", error }) => (
  <label style={{ display: "flex", flexDirection: "column", gap: 6 }}>
    {label && <span style={{ fontSize: 12, fontWeight: 600, color: "var(--ll-fg-muted)" }}>{label}</span>}
    <div style={{ position: "relative" }}>
      {prefix && (
        <span style={{ position: "absolute", left: 14, top: "50%", transform: "translateY(-50%)",
                       color: "var(--ll-fg-muted)", fontWeight: 600 }}>{prefix}</span>
      )}
      <input type={type} value={value || ""} onChange={e => onChange?.(e.target.value)} placeholder={placeholder}
        style={{
          width: "100%", boxSizing: "border-box", font: "inherit", fontSize: 15,
          padding: prefix ? "12px 14px 12px 28px" : "12px 14px",
          borderRadius: 10, border: `1px solid ${error ? "var(--ll-status-sold)" : "var(--ll-border-strong)"}`,
          background: "var(--ll-surface)", color: "var(--ll-fg)", outline: "none",
          fontFamily: prefix ? "var(--ll-font-mono)" : "inherit",
          fontVariantNumeric: prefix ? "tabular-nums" : "normal",
        }}
      />
    </div>
    {error ? (
      <span style={{ fontSize: 12, color: "var(--ll-status-sold-fg)" }}>{error}</span>
    ) : hint ? (
      <span style={{ fontSize: 11, color: "var(--ll-fg-subtle)" }}>{hint}</span>
    ) : null}
  </label>
);

// ============================================================
// Status chip
// ============================================================
const StatusChip = ({ status, shape = "pill", children }) => {
  const map = {
    available: { bg: "var(--ll-status-available-bg)", fg: "var(--ll-status-available-fg)", label: "Available" },
    reserved:  { bg: "var(--ll-status-reserved-bg)",  fg: "var(--ll-status-reserved-fg)",  label: "Reserved" },
    sold:      { bg: "var(--ll-status-sold-bg)",      fg: "var(--ll-status-sold-fg)",      label: "Sold" },
    hold:      { bg: "var(--ll-status-hold-bg)",      fg: "var(--ll-status-hold-fg)",      label: "On hold" },
    paid:      { bg: "var(--ll-status-available-bg)", fg: "var(--ll-status-available-fg)", label: "Paid" },
    due:       { bg: "var(--ll-status-reserved-bg)",  fg: "var(--ll-status-reserved-fg)",  label: "Due" },
    overdue:   { bg: "var(--ll-status-sold-bg)",      fg: "var(--ll-status-sold-fg)",      label: "Overdue" },
    upcoming:  { bg: "var(--ll-paper-100)",           fg: "var(--ll-fg-muted)",            label: "Upcoming" },
  }[status] || { bg: "var(--ll-paper-100)", fg: "var(--ll-fg-muted)", label: status };
  return (
    <span style={{
      display: "inline-flex", alignItems: "center", gap: 5,
      padding: "3px 8px", borderRadius: shape === "pill" ? 999 : 6,
      background: map.bg, color: map.fg, fontSize: 11, fontWeight: 600, whiteSpace: "nowrap",
    }}>
      <span style={{ width: 5, height: 5, borderRadius: 999, background: "currentColor" }} />
      {children || map.label}
    </span>
  );
};

// ============================================================
// Card
// ============================================================
const Card = ({ children, onClick, style, padded = true }) => (
  <div onClick={onClick} style={{
    background: "var(--ll-surface)", border: "1px solid var(--ll-border)", borderRadius: 12,
    boxShadow: "var(--ll-shadow-1)", padding: padded ? 16 : 0,
    cursor: onClick ? "pointer" : "default", ...style,
  }}>{children}</div>
);

// ============================================================
// Amount display
// ============================================================
const Amount = ({ value, size = "md", color = "var(--ll-fg)", style }) => {
  const sizes = { sm: 15, md: 18, lg: 22, xl: 28, xxl: 36 };
  return (
    <span style={{
      fontFamily: "var(--ll-font-sans)", fontWeight: 700, letterSpacing: "-0.01em",
      fontVariantNumeric: "tabular-nums", fontSize: sizes[size], color, ...style,
    }}>{window.formatINR(value)}</span>
  );
};

// ============================================================
// Sheet (bottom)
// ============================================================
const Sheet = ({ open, onClose, title, children }) => {
  if (!open) return null;
  return (
    <div style={{ position: "absolute", inset: 0, zIndex: 50, display: "flex", flexDirection: "column", justifyContent: "flex-end" }}>
      <div onClick={onClose} style={{ position: "absolute", inset: 0, background: "rgba(42,31,20,0.45)", animation: "llFade 180ms ease-out" }} />
      <div style={{
        position: "relative", background: "var(--ll-surface)",
        borderTopLeftRadius: 20, borderTopRightRadius: 20,
        boxShadow: "var(--ll-shadow-3)", padding: "12px 20px 24px", maxHeight: "85%",
        overflowY: "auto", animation: "llSlideUp 220ms cubic-bezier(0.22,1,0.36,1)",
      }}>
        <div style={{ width: 36, height: 4, background: "var(--ll-border-strong)", borderRadius: 2, margin: "0 auto 12px" }} />
        {title && <div style={{ fontWeight: 700, fontSize: 18, color: "var(--ll-fg)", marginBottom: 12 }}>{title}</div>}
        {children}
      </div>
      <style>{`
        @keyframes llFade { from { opacity:0 } to { opacity:1 } }
        @keyframes llSlideUp { from { transform: translateY(20px); opacity: 0 } to { transform: translateY(0); opacity: 1 } }
      `}</style>
    </div>
  );
};

// ============================================================
// ListRow — generic tappable list row
// ============================================================
const ListRow = ({ icon, title, subtitle, right, onClick, rightChip }) => (
  <div onClick={onClick} style={{
    display: "flex", alignItems: "center", gap: 12, padding: "12px 0",
    borderBottom: "1px solid var(--ll-border)", cursor: onClick ? "pointer" : "default",
  }}>
    {icon && (
      <div style={{ width: 36, height: 36, borderRadius: 10, background: "var(--ll-paper-100)",
                    display: "flex", alignItems: "center", justifyContent: "center", color: "var(--ll-fg-muted)" }}>
        <Icon name={icon} size={18} />
      </div>
    )}
    <div style={{ flex: 1, minWidth: 0 }}>
      <div style={{ fontWeight: 600, fontSize: 14, color: "var(--ll-fg)" }}>{title}</div>
      {subtitle && <div style={{ fontSize: 12, color: "var(--ll-fg-muted)", marginTop: 2 }}>{subtitle}</div>}
    </div>
    {rightChip}
    {right && <span style={{ fontWeight: 600, fontSize: 14, color: "var(--ll-fg)", fontFamily: "var(--ll-font-sans)", fontVariantNumeric: "tabular-nums" }}>{right}</span>}
    {onClick && <Icon name="chevR" size={16} color="var(--ll-fg-subtle)" />}
  </div>
);

Object.assign(window, { Icon, Button, Field, StatusChip, Card, Amount, Sheet, ListRow });
