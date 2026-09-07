import { test, expect } from '@playwright/test';
import { loadPrototype } from './test_helpers.js';

test.describe('Anti-Neglect Balancer & Queue Management', () => {
  test.beforeEach(async ({ page }) => {
    await loadPrototype(page);
  });

  test('Queue balancing algorithm prioritizes never-interacted, oldest, and least interacted', async ({ page }) => {
    const queueOrder = await page.evaluate(() => {
      window.__PRAYER_APP__.state.entities = [
        { id: 1, name: 'Alice (Frequently Prayed)', root: 'PEOPLE', interacted_count: 10, last_interacted_at: '2026-09-06T10:00:00Z', is_historic: false },
        { id: 2, name: 'Bob (Never Prayed)', root: 'PEOPLE', interacted_count: 0, last_interacted_at: null, is_historic: false },
        { id: 3, name: 'Charlie (Prayed Long Ago)', root: 'PEOPLE', interacted_count: 2, last_interacted_at: '2026-08-01T10:00:00Z', is_historic: false },
        { id: 4, name: 'Diana (Prayed Recently)', root: 'PEOPLE', interacted_count: 1, last_interacted_at: '2026-09-05T10:00:00Z', is_historic: false }
      ];

      window.__PRAYER_APP__.state.prayers = [
        { id: 101, entity_id: 1, title: 'Point 1', body: 'Body', status: 'ACTIVE' },
        { id: 102, entity_id: 2, title: 'Point 2', body: 'Body', status: 'ACTIVE' },
        { id: 103, entity_id: 3, title: 'Point 3', body: 'Body', status: 'ACTIVE' },
        { id: 104, entity_id: 4, title: 'Point 4', body: 'Body', status: 'ACTIVE' }
      ];

      window.__PRAYER_APP__.buildBalancedPrayerQueue();
      return window.__PRAYER_APP__.state.prayerQueue.map(e => e.name);
    });

    expect(queueOrder[0]).toBe('Bob (Never Prayed)');
    expect(queueOrder[1]).toBe('Charlie (Prayed Long Ago)');
    expect(queueOrder[2]).toBe('Diana (Prayed Recently)');
    expect(queueOrder[3]).toBe('Alice (Frequently Prayed)');
  });

  test('Advancing topic silently increments interacted_count and updates last_interacted_at', async ({ page }) => {
    await page.locator('#btn-home-pray').click();

    // Check entity at index 1 before advancing to it
    const nextEntityBefore = await page.evaluate(() => {
      const nextEnt = window.__PRAYER_APP__.state.prayerQueue[1];
      return { id: nextEnt.id, count: nextEnt.interacted_count, time: nextEnt.last_interacted_at };
    });

    // Advance to index 1
    await page.evaluate(() => window.__PRAYER_APP__.nextPrayerTopic());

    const nextEntityAfter = await page.evaluate((id) => {
      const ent = window.__PRAYER_APP__.state.entities.find(e => e.id === id);
      return { id: ent.id, count: ent.interacted_count, time: ent.last_interacted_at };
    }, nextEntityBefore.id);

    expect(nextEntityAfter.count).toBe(nextEntityBefore.count + 1);
    expect(new Date(nextEntityAfter.time).getTime()).toBeGreaterThanOrEqual(new Date(nextEntityBefore.time || 0).getTime());
  });

  test('Zero-state fallback: If personal points empty, draws from Historic Prayers', async ({ page }) => {
    const queueNames = await page.evaluate(() => {
      // Deactivate all non-historic prayers
      window.__PRAYER_APP__.state.prayers.forEach(p => {
        if (p.entity_id !== 7) p.status = 'ANSWERED';
      });
      window.__PRAYER_APP__.buildBalancedPrayerQueue();
      return window.__PRAYER_APP__.state.prayerQueue.map(e => e.name);
    });

    expect(queueNames).toContain('Historic Prayers');
  });
});
