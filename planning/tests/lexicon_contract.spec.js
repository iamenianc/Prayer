import { test, expect } from '@playwright/test';
import { loadPrototype } from './test_helpers.js';

test.describe('Lexicon, Devotional Terminology & Contextual Privacy Contract', () => {
  test.beforeEach(async ({ page }) => {
    await loadPrototype(page);
  });

  test('Strict prohibition of clinical, database, or engineering terms in user-facing UI text', async ({ page }) => {
    const forbidden = ['target', 'entity', 'ticket', 'commit', 'sqlite', 'database', 'pipeline'];
    const screens = [
      'screen-home',
      'screen-pray',
      'screen-journal',
      'screen-entity-detail',
      'screen-log-step0',
      'screen-log-step1',
      'screen-log-direct',
      'screen-log-guide',
      'screen-edit-petition',
      'screen-settings'
    ];

    for (const screenId of screens) {
      await page.evaluate((s) => window.__PRAYER_APP__.showScreen(s), screenId);
      const text = await page.locator(`#${screenId}`).innerText();

      for (const word of forbidden) {
        const regex = new RegExp(`\\b${word}\\b`, 'i');
        const match = regex.exec(text);
        if (match) {
          throw new Error(`Forbidden term "${word}" found in screen ${screenId}: "${match.input.substring(0, 100)}..."`);
        }
      }
    }
  });

  test('Absolute prohibition of sequential or ordinal numbering (Point 1, Item 1, Point 1 of N)', async ({ page }) => {
    const ordinalRegex = /\b(point \d+|item \d+|petition \d+|\d+ of \d+)\b/i;
    const screens = ['screen-home', 'screen-pray', 'screen-journal', 'screen-entity-detail'];

    for (const screenId of screens) {
      await page.evaluate((s) => window.__PRAYER_APP__.showScreen(s), screenId);
      const text = await page.locator(`#${screenId}`).innerText();
      const match = ordinalRegex.exec(text);
      expect(match).toBeNull();
    }
  });

  test('Minimal contextual data exposure: zero queue progression counters or administrative tallies', async ({ page }) => {
    await page.locator('#btn-home-pray').click();
    const prayText = await page.locator('#screen-pray').innerText();

    // Verify absence of "Topic 1 of N" or "(3 active, 2 ans)"
    expect(prayText).not.toMatch(/topic \d+ of \d+/i);
    expect(prayText).not.toMatch(/\(\d+ active/i);
  });

  test('Language and dialect selector configures English AU/UK vs US English', async ({ page }) => {
    await page.locator('#btn-home-journal').click();
    await page.locator('#top-bar-action-right').click(); // Open Settings
    await expect(page.locator('#screen-settings')).toBeVisible();

    // Default is en-AU
    let lang = await page.evaluate(() => document.documentElement.lang);
    expect(lang).toBe('en-AU');

    // Switch to US English
    await page.locator('#opt-lang-us').click();
    lang = await page.evaluate(() => document.documentElement.lang);
    expect(lang).toBe('en-US');

    // Switch back to AU/UK English
    await page.locator('#opt-lang-uk').click();
    lang = await page.evaluate(() => document.documentElement.lang);
    expect(lang).toBe('en-AU');
  });
});
