/*
 * Titanium SDK
 * Copyright TiDev, Inc. 04/07/2022-Present. All Rights Reserved.
 * Licensed under the terms of the Apache Public License
 * Please see the LICENSE included with this distribution for details.
 */
/* eslint-env mocha */
/* eslint no-unused-expressions: "off" */
'use strict';
var should = require('./utilities/assertions');

describe.android('Titanium.App.Android', function () {
	it('appVersionCode', function () {
		// Defined in file: ./platform/android/build.gradle
		should(Ti.App.Android.appVersionCode).not.be.undefined();
		should(Ti.App.Android.appVersionCode).be.a.Number();
		should(Ti.App.Android.appVersionCode).be.eql(101);
	});

	it('appVersionName', function () {
		// Defined in file: ./platform/android/AndroidManifest.xml
		should(Ti.App.Android.appVersionName).not.be.undefined();
		should(Ti.App.Android.appVersionName).be.a.String();
		should(Ti.App.Android.appVersionName).be.eql('v1.01');
	});

	it('launchIntent', function () {
		const launchIntent = Ti.App.Android.launchIntent;
		should(launchIntent).not.be.undefined();
		should(launchIntent).be.a.Object();
		should(launchIntent.packageName).eql(Ti.App.id);
	});

	describe('unhandledrejection', function () {
		this.timeout(5000);

		function collectReasonsContaining(marker, durationMs, callback) {
			const reasons = [];
			const listener = e => {
				if (e.reason.includes(marker)) {
					reasons.push(e.reason);
				}
			};
			Ti.App.addEventListener('unhandledrejection', listener);
			setTimeout(() => {
				Ti.App.removeEventListener('unhandledrejection', listener);
				callback(reasons);
			}, durationMs);
		}

		it('is not fired when an async function throws before its first await and the caller catches', function (finish) {
			collectReasonsContaining('thrown-before-await', 1000, reasons => {
				try {
					should(reasons).be.empty();
					finish();
				} catch (err) {
					finish(err);
				}
			});

			async function throwBeforeAwait() {
				throw new Error('thrown-before-await');
			}
			(async () => {
				try {
					await throwBeforeAwait();
				} catch {
					// Handled.
				}
			})();
		});

		it('is not fired when a handler is attached in a later microtask', function (finish) {
			collectReasonsContaining('handled-in-microtask', 1000, reasons => {
				try {
					should(reasons).be.empty();
					finish();
				} catch (err) {
					finish(err);
				}
			});

			const rejected = Promise.reject(new Error('handled-in-microtask'));
			Promise.resolve().then(() => rejected.catch(() => {}));
		});

		it('is fired for a rejection nothing handles', function (finish) {
			const listener = e => {
				if (!e.reason.includes('never-handled')) {
					return;
				}
				Ti.App.removeEventListener('unhandledrejection', listener);
				try {
					should(e.name).eql('Error');
					should(e.message).eql('never-handled');
					finish();
				} catch (err) {
					finish(err);
				}
			};
			Ti.App.addEventListener('unhandledrejection', listener);

			Promise.reject(new Error('never-handled'));
		});
	});
});
