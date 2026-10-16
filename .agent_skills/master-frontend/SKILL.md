---
name: master-frontend
description: Rules for writing and changing the Axelix Master console UI — the React + TypeScript front-end in `master/front-end/` that Axelix Master serves. Use this skill whenever you create, modify, or review any `.tsx`, `.ts`, or `.css` file under `master/front-end/src/` — new pages, components, dashboards, charts, styles, or a one-line color tweak — even if the user only says "add a button to the Beans page", "restyle this card", or "fix the UI".
---

# Axelix Master Front-end

Guide for writing front-end code in **`master/front-end/`** — the React + TypeScript console UI that is bundled into the Axelix Master JAR and served by it.

The principles below are mandatory. Follow each of them for every change you make under `master/front-end/src/`.

## Principle 1: Use the root CSS color variables, not inline hex colors

The root color palette lives in the `:root` block of `master/front-end/src/index.css` (`--axelix-primary-green`, `--axelix-light-grey`, `--axelix-primary-red`, …). Whenever you need a color, use one of those variables — never write a hex literal (`#00ab55`), `rgb(...)`, or a named color (`white`) in its place.

**Before you pick a color, read the `:root` block of `index.css`.** Most of the time the color you want is already there.

### In CSS (`styles.module.css` and friends)

```css
/* Wrong */
.Card {
    border: 1px solid #d9d9d9;
    background-color: #fafafa;
}

/* Right */
.Card {
    border: 1px solid var(--axelix-medium-grey);
    background-color: var(--axelix-extra-light-grey);
}
```

A component may define its own local custom properties, but they must resolve to root variables — see `pages/Loggers/Logger/LoggerScheduler/LoggerTimePicker/styles.module.css`:

```css
--tp-primary: var(--axelix-primary-green);
```

### In TSX / TS

`var(--…)` works in inline styles and in SVG attributes (Recharts `fill`, `stroke`, etc.), so use it there too:

```tsx
// Wrong
<Line stroke="#838383" />
const LINE_COLOR = "#1890ff";

// Right
<Line stroke="var(--axelix-bold-grey)" />
const LINE_COLOR = "var(--axelix-primary-blue)";
```

The one real exception: places where a library must compute with the color itself and cannot resolve a CSS variable — e.g. the Ant Design `ThemeConfig` tokens in `App.tsx` (`colorPrimary: "#00ab55"`), which antd uses to derive hover/active shades. A literal is acceptable there; keep it equal to the matching root variable's value.

### Adding a new root variable

If — after actually checking `:root` — no existing variable fits, you may add one. This should be rare, so **think twice first**:

- Is an existing variable close enough that a designer would not tell the difference? Use it.
- Is it a tint of an existing color? Check for the `-light-` / `-dark-` variant (e.g. `--axelix-primary-light-green`) before inventing one.

If you still need a new one:

- Add it to the `:root` block in `master/front-end/src/index.css` — not in a component's CSS module.
- Follow the existing naming, e.g. `--axelix-<intensity>-<color>` (`--axelix-primary-purple`, `--axelix-light-purple`).
- Mention the new variable to the user in your summary so it gets a conscious review.

### Pre-existing hex literals

There are still hex literals in the codebase. Don't rewrite them as a drive-by — only fix colors in the code you are actually changing.
