import { defineConfig } from '@playwright/test';
import path from 'path';
import { fileURLToPath } from 'url';

const __filename = fileURLToPath(import.meta.url);
const __dirname = path.dirname(__filename);
export const PROTOTYPE_URL = 'file://' + path.resolve(__dirname, '../prototype.html').replace(/\\/g, '/');

export default defineConfig({
  testDir: './',
  testMatch: /.*\.spec\.js/,
  timeout: 30000,
  forbidOnly: !!process.env.CI,
  retries: 0,
  workers: 1,
  reporter: [['list']],
  use: {
    channel: 'chrome', // Use system installed Chrome
    hasTouch: true,
    isMobile: true,
    launchOptions: {
      args: ['--allow-file-access-from-files']
    }
  },
  projects: [
    {
      name: 'Mobile-Standard-390x844',
      use: {
        viewport: { width: 390, height: 844 },
      },
    },
    {
      name: 'Mobile-Compact-360x740',
      use: {
        viewport: { width: 360, height: 740 },
      },
    },
    {
      name: 'Mobile-Large-428x926',
      use: {
        viewport: { width: 428, height: 926 },
      },
    }
  ],
});
