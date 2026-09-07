import { test, expect } from '@playwright/test';
import { loadPrototype } from './test_helpers.js';

test.describe('1. Strict Geometry & Zero-Gap Contiguity Tests', () => {
  test.beforeEach(async ({ page }) => {
    await loadPrototype(page);
  });

  test('Universal 0px border-radius enforced across all interactive elements and containers', async ({ page }) => {
    const nonZeroElements = await page.evaluate(() => {
      const bad = [];
      const els = document.querySelectorAll('*');
      for (const el of els) {
        const style = window.getComputedStyle(el);
        if (style.borderRadius && style.borderRadius !== '0px') {
          bad.push({ tag: el.tagName, className: el.className, id: el.id, radius: style.borderRadius });
        }
      }
      return bad;
    });
    expect(nonZeroElements).toEqual([]);
  });

  test('Home screen action slabs share 0px margin and direct contiguous 1px hairline boundary', async ({ page }) => {
    const slabs = page.locator('#screen-home .home-slab');
    await expect(slabs).toHaveCount(3);

    const rects = await slabs.evaluateAll(elements => elements.map(el => {
      const r = el.getBoundingClientRect();
      const style = window.getComputedStyle(el);
      return {
        top: r.top,
        bottom: r.bottom,
        height: r.height,
        marginTop: style.marginTop,
        marginBottom: style.marginBottom,
        borderBottomWidth: style.borderBottomWidth
      };
    }));

    // Check 0px margin
    for (const r of rects) {
      expect(r.marginTop).toBe('0px');
      expect(r.marginBottom).toBe('0px');
    }

    // Check direct adjacency (slab 0 bottom touches slab 1 top within 1px)
    expect(Math.abs(rects[1].top - rects[0].bottom)).toBeLessThanOrEqual(1.5);
    expect(Math.abs(rects[2].top - rects[1].bottom)).toBeLessThanOrEqual(1.5);
  });

  test('Zero drop-shadow or elevation on cards, buttons, and slabs', async ({ page }) => {
    const shadows = await page.evaluate(() => {
      const bad = [];
      const els = document.querySelectorAll('.home-slab, .entity-tile, .detail-petition-card, .candidate-card, .btn-submit, .btn-secondary');
      for (const el of els) {
        const style = window.getComputedStyle(el);
        if (style.boxShadow !== 'none' && style.boxShadow !== '') {
          bad.push({ class: el.className, shadow: style.boxShadow });
        }
      }
      return bad;
    });
    expect(shadows).toEqual([]);
  });
});

test.describe('2. Visual Modes & Monochrome Tokens', () => {
  test.beforeEach(async ({ page }) => {
    await loadPrototype(page);
  });

  test('Morning Light mode enforces pure white background (#FFFFFF) and dark text (#111111)', async ({ page }) => {
    await page.evaluate(() => {
      document.documentElement.setAttribute('data-theme', 'light');
    });

    const tokens = await page.evaluate(() => {
      const style = window.getComputedStyle(document.documentElement);
      return {
        bg: style.getPropertyValue('--bg').trim().toUpperCase(),
        text: style.getPropertyValue('--text').trim().toUpperCase(),
        border: style.getPropertyValue('--border').trim().toUpperCase()
      };
    });

    expect(tokens.bg).toBe('#FFFFFF');
    expect(tokens.text).toBe('#111111');
    expect(tokens.border).toBe('#E0E0E0');
  });

  test('Quiet Night mode enforces pure black background (#000000) and white text (#FFFFFF)', async ({ page }) => {
    await page.evaluate(() => {
      document.documentElement.setAttribute('data-theme', 'night');
    });

    const tokens = await page.evaluate(() => {
      const style = window.getComputedStyle(document.documentElement);
      return {
        bg: style.getPropertyValue('--bg').trim().toUpperCase(),
        text: style.getPropertyValue('--text').trim().toUpperCase(),
        border: style.getPropertyValue('--border').trim().toUpperCase()
      };
    });

    expect(tokens.bg).toBe('#000000');
    expect(tokens.text).toBe('#FFFFFF');
    expect(tokens.border).toBe('#262626');
  });
});

test.describe('3. Three-Tier Text Scale Typography', () => {
  test.beforeEach(async ({ page }) => {
    await loadPrototype(page);
  });

  test('Large scale (Default) sets font-slab 28px, font-title 22px, font-body 18px', async ({ page }) => {
    await page.evaluate(() => document.documentElement.setAttribute('data-text-scale', 'large'));
    const styles = await page.evaluate(() => {
      const style = window.getComputedStyle(document.documentElement);
      return {
        slab: style.getPropertyValue('--font-slab').trim(),
        title: style.getPropertyValue('--font-title').trim(),
        body: style.getPropertyValue('--font-body').trim()
      };
    });
    expect(styles.slab).toBe('28px');
    expect(styles.title).toBe('22px');
    expect(styles.body).toBe('18px');
  });

  test('Regular scale sets font-slab 20px, font-title 16px, font-body 13px', async ({ page }) => {
    await page.evaluate(() => document.documentElement.setAttribute('data-text-scale', 'regular'));
    const styles = await page.evaluate(() => {
      const style = window.getComputedStyle(document.documentElement);
      return {
        slab: style.getPropertyValue('--font-slab').trim(),
        title: style.getPropertyValue('--font-title').trim(),
        body: style.getPropertyValue('--font-body').trim()
      };
    });
    expect(styles.slab).toBe('20px');
    expect(styles.title).toBe('16px');
    expect(styles.body).toBe('13px');
  });

  test('Compact scale sets font-slab 15px, font-title 12.5px, font-body 10.5px', async ({ page }) => {
    await page.evaluate(() => document.documentElement.setAttribute('data-text-scale', 'compact'));
    const styles = await page.evaluate(() => {
      const style = window.getComputedStyle(document.documentElement);
      return {
        slab: style.getPropertyValue('--font-slab').trim(),
        title: style.getPropertyValue('--font-title').trim(),
        body: style.getPropertyValue('--font-body').trim()
      };
    });
    expect(styles.slab).toBe('15px');
    expect(styles.title).toBe('12.5px');
    expect(styles.body).toBe('10.5px');
  });
});
