import { test, expect } from '@playwright/test';
import { loadPrototype, simulateSwipe } from './test_helpers.js';

test.describe('Gesture Engine & Touch Interaction Tests', () => {
  test.beforeEach(async ({ page }) => {
    await loadPrototype(page);
  });

  test('Horizontal Swipe Left (ΔX <= -40px) advances to next topic in prayer sanctuary', async ({ page }) => {
    await page.locator('#btn-home-pray').click();
    await expect(page.locator('#screen-pray')).toBeVisible();

    const initialHeading = await page.locator('#pray-heading').textContent();

    // Emulate touch swipe left: Start (300, 400), End (100, 410) -> ΔX = -200px, ΔY = 10px
    await simulateSwipe(page, 300, 400, 100, 410);

    const newHeading = await page.locator('#pray-heading').textContent();
    expect(newHeading).not.toBe(initialHeading);
  });

  test('Horizontal Swipe Right (ΔX >= +40px) returns to previous topic in prayer sanctuary', async ({ page }) => {
    await page.locator('#btn-home-pray').click();

    // Advance first
    await simulateSwipe(page, 300, 400, 100, 410);
    const advancedHeading = await page.locator('#pray-heading').textContent();

    // Swipe right to return: Start (100, 400), End (300, 405) -> ΔX = +200px, ΔY = 5px
    await simulateSwipe(page, 100, 400, 300, 405);
    const returnedHeading = await page.locator('#pray-heading').textContent();
    expect(returnedHeading).not.toBe(advancedHeading);
  });

  test('Diagonal swipe (|ΔX| < 1.5 * |ΔY|) is rejected without changing topic', async ({ page }) => {
    await page.locator('#btn-home-pray').click();
    const initialHeading = await page.locator('#pray-heading').textContent();

    // Start (200, 300), End (150, 260) -> ΔX = -50px, ΔY = -40px -> ratio 1.25 < 1.5
    await simulateSwipe(page, 200, 300, 150, 260);

    const currentHeading = await page.locator('#pray-heading').textContent();
    expect(currentHeading).toBe(initialHeading);
  });

  test('Swipe Down (ΔY >= 60px) initiated in upper 150px exits prayer mode to Home', async ({ page }) => {
    await page.locator('#btn-home-pray').click();
    await expect(page.locator('#screen-pray')).toBeVisible();

    // Start (200, 60), End (205, 160) -> Ystart = 60 <= 150, ΔY = 100px >= 60
    await simulateSwipe(page, 200, 60, 205, 160);

    await expect(page.locator('#screen-home')).toBeVisible();
    await expect(page.locator('#screen-pray')).not.toBeVisible();
  });

  test('Home screen Swipe Left navigates directly to Journal', async ({ page }) => {
    await expect(page.locator('#screen-home')).toBeVisible();

    await page.evaluate(() => {
      const home = document.getElementById('screen-home');
      const touch1 = new Touch({ identifier: 1, target: home, clientX: 300, clientY: 400 });
      home.dispatchEvent(new TouchEvent('touchstart', { touches: [touch1], targetTouches: [touch1], changedTouches: [touch1], bubbles: true }));

      const touch2 = new Touch({ identifier: 1, target: home, clientX: 100, clientY: 405 });
      home.dispatchEvent(new TouchEvent('touchend', { touches: [], targetTouches: [], changedTouches: [touch2], bubbles: true }));
    });

    await expect(page.locator('#screen-journal')).toBeVisible();
  });
});
