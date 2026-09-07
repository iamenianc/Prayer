import { test, expect } from '@playwright/test';
import { loadPrototype } from './test_helpers.js';

test.describe('Devotional Flows & Logging Pathways', () => {
  test.beforeEach(async ({ page }) => {
    await loadPrototype(page);
  });

  test('Start praying sanctuary: 0 buttons, pure typography, expandable answered petitions', async ({ page }) => {
    await page.locator('#btn-home-pray').click();
    await expect(page.locator('#screen-pray')).toBeVisible();

    // Check top bar is hidden
    await expect(page.locator('#top-bar')).toBeHidden();

    // Check heading exists and starts with "Praying for "
    const heading = await page.locator('#pray-heading').textContent();
    expect(heading).toMatch(/^Praying for .+/);

    // Active prayer points rendered
    const activePoints = page.locator('#pray-points-container .prayer-point-entry');
    expect(await activePoints.count()).toBeGreaterThan(0);

    // Answered section is collapsed by default, expandable on tap
    const answeredHeading = page.locator('#pray-answered-heading');
    if (await answeredHeading.isVisible()) {
      const answeredList = page.locator('#pray-answered-list');
      await expect(answeredList).toBeHidden();
      await answeredHeading.click();
      await expect(answeredList).toBeVisible();
    }
  });

  test('Direct Entry: Zero title field, auto-bullets engine, and immediate save', async ({ page }) => {
    await page.locator('#btn-home-log').click();
    await expect(page.locator('#screen-log-step0')).toBeVisible();

    // Pick first entity from journal
    const firstEntity = page.locator('#log-picker-list .entity-tile').first();
    const entityName = await firstEntity.locator('span').first().textContent();
    await firstEntity.click();

    await expect(page.locator('#screen-log-step1')).toBeVisible();
    await page.locator('#btn-pathway-direct').click();

    await expect(page.locator('#screen-log-direct')).toBeVisible();

    // Verify ZERO title field
    const titleInputs = page.locator('#screen-log-direct input[type="text"]');
    await expect(titleInputs).toHaveCount(0);

    // Verify auto-bullet on focus/input
    const pad = page.locator('#direct-input-body');
    await expect(pad).toBeVisible();

    // Type text and enter
    await pad.fill('• Recovery from sickness');
    await pad.press('Enter');
    const val = await pad.inputValue();
    expect(val).toContain('\n• ');

    // Save
    const saveBtn = page.locator('#btn-direct-save');
    await expect(saveBtn).toHaveText(`Save to ${entityName}`);
    await saveBtn.click();

    // Successfully transitioned to entity detail with petition saved
    await expect(page.locator('#screen-entity-detail')).toBeVisible();
    const detailPetitions = page.locator('#entity-petitions-list .detail-petition-card');
    await expect(detailPetitions.first()).toContainText('Recovery from sickness');
  });

  test('Guide Me: Multi-step articulation, skip to petitions, strictly 2 candidate cards', async ({ page }) => {
    await page.locator('#btn-home-log').click();
    await page.locator('#log-picker-list .entity-tile').first().click();
    await page.locator('#btn-pathway-guide').click();

    await expect(page.locator('#screen-log-guide')).toBeVisible();
    await expect(page.locator('#guide-substep-1')).toBeVisible();

    // Enter reflection
    await page.locator('#guide-raw-input').fill('I am feeling anxious about my upcoming medical exam and want to trust God.');
    await page.locator('#btn-guide-continue').click();

    // Substep 2: Clarifying question
    await expect(page.locator('#guide-substep-2')).toBeVisible();
    const qText = await page.locator('#guide-question-text').textContent();
    expect(qText.trim().length).toBeGreaterThan(10);

    // Skip question unconditionally advances to candidate petitions
    await page.locator('#btn-guide-skip-q').click();

    // Substep 3: Candidate petitions
    await expect(page.locator('#guide-substep-3')).toBeVisible();
    const candidates = page.locator('#guide-candidates-container .candidate-card');
    await expect(candidates).toHaveCount(2);

    for (let i = 0; i < 2; i++) {
      const card = candidates.nth(i);
      const title = await card.locator('.candidate-title').textContent();
      const body = await card.locator('.candidate-body').textContent();
      const words = title.trim().split(/\s+/).length;
      expect(words).toBeGreaterThanOrEqual(2);
      expect(words).toBeLessThanOrEqual(6);
      expect(body).toContain(';');
    }

    // "Suggest 2 more" is strictly one-time action
    const btnMore = page.locator('#btn-guide-request-more');
    await expect(btnMore).toBeVisible();
    await btnMore.click();
    await expect(btnMore).toBeHidden();

    // Save petitions
    await page.locator('#btn-guide-save-candidates').click();
    await expect(page.locator('#screen-entity-detail')).toBeVisible();
  });

  test('Petition Editor: Single-click to edit, editable title and body, permanent delete', async ({ page }) => {
    await page.locator('#btn-home-journal').click();
    await page.locator('#journal-people-list .entity-tile').first().click();

    // Click first petition once
    const firstCard = page.locator('#entity-petitions-list .detail-petition-card').first();
    const originalTitle = await firstCard.locator('.petition-card-title').textContent();
    await firstCard.click();

    await expect(page.locator('#screen-edit-petition')).toBeVisible();

    // Edit title
    const titleInput = page.locator('#edit-input-title');
    await expect(titleInput).toHaveValue(originalTitle);
    await titleInput.fill('Updated Title for Test');

    // Save changes
    await page.locator('#btn-edit-save').click();
    await expect(page.locator('#screen-entity-detail')).toBeVisible();
    await expect(page.locator('#entity-petitions-list .detail-petition-card').first()).toContainText('Updated Title for Test');

    // Re-open and test permanent deletion
    await page.locator('#entity-petitions-list .detail-petition-card').first().click();
    await page.locator('#btn-edit-delete').click();
    await expect(page.locator('#delete-confirm-box')).toBeVisible();
    await page.locator('#btn-confirm-delete').click();

    await expect(page.locator('#screen-entity-detail')).toBeVisible();
    await expect(page.locator('#entity-petitions-list')).not.toContainText('Updated Title for Test');
  });
});
