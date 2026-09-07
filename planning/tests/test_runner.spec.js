import { test, expect } from '@playwright/test';
import path from 'path';
import { fileURLToPath } from 'url';

const __filename = fileURLToPath(import.meta.url);
const __dirname = path.dirname(__filename);
const RUNNER_URL = 'file://' + path.resolve(__dirname, '../test_runner.html').replace(/\\/g, '/');

test.describe('In-Browser Spec & Layout Validator (test_runner.html)', () => {
  // Use desktop viewport for test runner dashboard
  test.use({ viewport: { width: 1280, height: 900 }, isMobile: false, hasTouch: false });

  test('Executes full in-browser test battery with 100% pass rate and 0 failures', async ({ page }) => {
    page.on('pageerror', err => console.error(`[PAGE ERROR]: ${err.message}`));
    page.on('dialog', dialog => dialog.accept());

    await page.goto(RUNNER_URL);

    // Wait for the prototype iframe to load
    const iframe = page.frameLocator('#viewport-iframe');
    await expect(iframe.locator('#screen-home')).toBeVisible({ timeout: 10000 });

    // Trigger runValidationBattery via UI button
    const btnRunAll = page.locator('#btn-run-all');
    await btnRunAll.click();

    // Wait until test run finishes (button says "Re-Run Validation Battery" and is re-enabled)
    await expect(btnRunAll).toHaveText('Re-Run Validation Battery', { timeout: 30000 });
    await expect(btnRunAll).toBeEnabled();

    // Verify scorecard metrics
    const scores = await page.evaluate(() => window.__TEST_RUNNER__.getScores());

    if (scores.failed > 0) {
      const failures = await page.evaluate(() => {
        return Array.from(document.querySelectorAll('.test-item')).filter(el => el.querySelector('.status-fail')).map(el => {
          return {
            desc: el.querySelector('.test-desc > div:first-child')?.textContent,
            error: el.querySelector('.test-error')?.textContent
          };
        });
      });
      console.log('FAILURES ENCOUNTERED IN TEST RUNNER:', JSON.stringify(failures, null, 2));
    }

    expect(scores.total).toBeGreaterThan(15);
    expect(scores.passed).toBe(scores.total);
    expect(scores.failed).toBe(0);
    expect(scores.rate).toBe('100%');
  });
});
