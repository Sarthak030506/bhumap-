// Shell: top app bar + bottom tab bar
/* global React, Icon */
const { useState: useStateShell } = React;

const AppBar = ({ title, subtitle, onBack, right, variant = "paper" }) => (
  <div style={{
    display: "flex", alignItems: "center", gap: 12,
    padding: "12px 16px", height: 56, boxSizing: "border-box",
    background: variant === "dark" ? "var(--ll-soil-900)" : "var(--ll-bg)",
    color: variant === "dark" ? "var(--ll-fg-on-dark)" : "var(--ll-fg)",
    borderBottom: variant === "dark" ? "1px solid rgba(255,255,255,.06)" : "1px solid var(--ll-border)",
    position: "sticky", top: 0, zIndex: 10,
  }}>
    {onBack && (
      <button onClick={onBack} style={{
        width: 36, height: 36, borderRadius: 10, border: "none",
        background: variant === "dark" ? "rgba(255,255,255,.06)" : "var(--ll-paper-100)",
        color: "inherit", display: "flex", alignItems: "center", justifyContent: "center", cursor: "pointer",
      }}><Icon name="back" size={18} /></button>
    )}
    <div style={{ flex: 1, minWidth: 0 }}>
      <div style={{ fontWeight: 700, fontSize: 17, lineHeight: 1.2, letterSpacing: "-0.01em" }}>{title}</div>
      {subtitle && <div style={{ fontSize: 12, opacity: 0.65, marginTop: 2 }}>{subtitle}</div>}
    </div>
    {right}
  </div>
);

const TabBar = ({ tabs, active, onChange }) => (
  <div style={{
    position: "sticky", bottom: 0, background: "var(--ll-surface)",
    borderTop: "1px solid var(--ll-border)",
    display: "grid", gridTemplateColumns: `repeat(${tabs.length}, 1fr)`,
    padding: "8px 8px 12px", gap: 4, zIndex: 10,
  }}>
    {tabs.map(t => {
      const isActive = active === t.id;
      return (
        <div key={t.id} onClick={() => onChange(t.id)} style={{
          display: "flex", flexDirection: "column", alignItems: "center", gap: 3,
          padding: "6px 4px", borderRadius: 10, cursor: "pointer",
          background: isActive ? "var(--ll-primary-soft)" : "transparent",
          color: isActive ? "var(--ll-primary)" : "var(--ll-fg-muted)",
          transition: "background 180ms, color 180ms",
        }}>
          <Icon name={t.icon} size={22} strokeWidth={isActive ? 2 : 1.75} />
          <span style={{ fontSize: 10.5, fontWeight: 600 }}>{t.label}</span>
        </div>
      );
    })}
  </div>
);

// Scrollable screen body with safe area padding
const Scroll = ({ children, padded = true, style }) => (
  <div style={{
    flex: 1, overflowY: "auto", padding: padded ? "16px 16px 24px" : 0,
    background: "var(--ll-bg)", ...style,
  }}>{children}</div>
);

Object.assign(window, { AppBar, TabBar, Scroll });
