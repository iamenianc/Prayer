import path from 'path';
import { fileURLToPath } from 'url';

const __filename = fileURLToPath(import.meta.url);
const __dirname = path.dirname(__filename);
export const PROTOTYPE_PATH = 'file://' + path.resolve(__dirname, '../prototype.html').replace(/\\/g, '/');

export async function loadPrototype(page) {
  await page.goto(PROTOTYPE_PATH);
  await page.waitForFunction(() => typeof window.__PRAYER_APP__ !== 'undefined');
}

export async function getComputedStyleProp(page, selector, prop) {
  return await page.$eval(selector, (el, p) => {
    return window.getComputedStyle(el).getPropertyValue(p);
  }, prop);
}

export async function simulateSwipe(page, startX, startY, endX, endY) {
  await page.evaluate(({ sx, sy, ex, ey }) => {
    window.__PRAYER_APP__.handlePrayGesture(sx, sy, ex, ey, 150);
  }, { sx: startX, sy: startY, ex: endX, ey: endY });
}
