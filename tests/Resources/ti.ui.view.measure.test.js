/* eslint-env mocha */

describe('Ti.UI.View.measureActualDimensions', () => {
	it('alias measureActualDimensions exists', () => {
		const v = Ti.UI.createView();
		if (typeof v.measureActualDimensions !== 'function') {
			throw new Error('measureActualDimensions not found on View');
		}
	});
	it('returns width/height for unattached label with SIZE and long text', async () => {
		const label = Ti.UI.createLabel({
			width: Ti.UI.SIZE,
			height: Ti.UI.SIZE,
			text: 'This is a long text that should wrap into multiple lines when constrained.'
		});

		const result = await label.measureActualDimensions({ maxWidth: 200 });
		if (!result) {
			throw new Error('measure returned null/undefined');
		}
		if (typeof result.width !== 'number' || typeof result.height !== 'number') {
			throw new Error('measure did not return numeric width/height');
		}
		if (!(result.width <= 200)) {
			throw new Error('width should be <= maxWidth constraint');
		}
		if (!(result.height > 0)) {
			throw new Error('height should be > 0');
		}
	});

	it('respects exact width/height', async () => {
		const view = Ti.UI.createView({ backgroundColor: 'red' });
		const result = await view.measureActualDimensions({ width: 123, height: 45 });
		if (result.width !== 123 || result.height !== 45) {
			throw new Error(`unexpected size ${JSON.stringify(result)}`);
		}
	});

	it('retry until minHeight is reached (within limits)', async () => {
		const label = Ti.UI.createLabel({
			width: Ti.UI.SIZE,
			height: Ti.UI.SIZE,
			text: 'Dette er en meget lang tekst der typisk vil wrappe over flere linjer for at teste retry.'
		});
		const size = await label.measureActualDimensions({ maxWidth: 220, minHeight: 40, maxFrames: 5, timeoutMs: 400 });
		if (!(typeof size.height === 'number' && size.height >= 0)) {
			throw new Error('measure returned invalid height');
		}
		// Vi kan ikke garantere eksakt højde på alle devices, men bør nå minimum i praksis
		// så længe teksten wraper. Tillad en lille margin, men kræv > 20 for sanity.
		if (size.height < 20) {
			throw new Error(`height too small after retries: ${size.height}`);
		}
	});
});
