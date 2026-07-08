/*
 * Titanium SDK
 * Copyright TiDev, Inc. 04/07/2022-Present. All Rights Reserved.
 * Licensed under the terms of the Apache Public License
 * Please see the LICENSE included with this distribution for details.
 */
/* eslint-env mocha */

/* eslint no-unused-expressions: "off" */
'use strict';
const should = require('./utilities/assertions');

describe('Intl.RelativeTimeFormat', () => {
	it('exists as a constructor', () => {
		should(Intl.RelativeTimeFormat).not.be.undefined();
		should(Intl.RelativeTimeFormat).be.a.Function();
	});

	describe('#supportedLocalesOf()', () => {
		it('is a Function', () => {
			should(Intl.RelativeTimeFormat.supportedLocalesOf).not.be.undefined();
			should(Intl.RelativeTimeFormat.supportedLocalesOf).be.a.Function();
		});

		it('returns array for valid locale', () => {
			const locales = Intl.RelativeTimeFormat.supportedLocalesOf('en-US');
			should(locales).be.an.Array();
			should(locales.length).be.aboveOrEqual(1);
		});

		it('returns empty array for invalid locale', () => {
			const locales = Intl.RelativeTimeFormat.supportedLocalesOf('xx-XX');
			should(locales).be.an.Array();
			should(locales.length).be.eql(0);
		});
	});

	describe('#format()', () => {
		it('formats past days with numeric: always', () => {
			const rtf = new Intl.RelativeTimeFormat('en', { numeric: 'always' });
			const result = rtf.format(-1, 'day');
			should(result).be.a.String();
			should(result.length).be.above(0);
			should(result).be.eql('1 day ago');
		});

		it('formats future days with numeric: always', () => {
			const rtf = new Intl.RelativeTimeFormat('en', { numeric: 'always' });
			const result = rtf.format(1, 'day');
			should(result).be.a.String();
			should(result).be.eql('in 1 day');
		});

		it('formats past days with numeric: auto', () => {
			const rtf = new Intl.RelativeTimeFormat('en', { numeric: 'auto' });
			const result = rtf.format(-1, 'day');
			should(result).be.a.String();
			should(result).be.eql('yesterday');
		});

		it('formats future days with numeric: auto', () => {
			const rtf = new Intl.RelativeTimeFormat('en', { numeric: 'auto' });
			const result = rtf.format(1, 'day');
			should(result).be.a.String();
			should(result).be.eql('tomorrow');
		});

		it('formats multiple units', () => {
			const rtf = new Intl.RelativeTimeFormat('en', { numeric: 'always' });
			should(rtf.format(-3, 'hour')).be.a.String();
			should(rtf.format(5, 'minute')).be.a.String();
			should(rtf.format(-2, 'week')).be.a.String();
			should(rtf.format(1, 'month')).be.a.String();
			should(rtf.format(-1, 'year')).be.a.String();
			should(rtf.format(2, 'second')).be.a.String();
			should(rtf.format(-1, 'quarter')).be.a.String();
		});

		it('accepts plural unit names', () => {
			const rtf = new Intl.RelativeTimeFormat('en', { numeric: 'always' });
			should(rtf.format(-3, 'days')).be.eql('3 days ago');
			should(rtf.format(2, 'hours')).be.eql('in 2 hours');
		});

		it('formats in different locales', () => {
			const rtf = new Intl.RelativeTimeFormat('de', { numeric: 'always' });
			const result = rtf.format(-1, 'day');
			should(result).be.a.String();
			should(result.length).be.above(0);
			// German: "vor 1 Tag"
			should(result).be.eql('vor 1 Tag');
		});
	});

	describe('#resolvedOptions()', () => {
		it('returns expected default options', () => {
			const rtf = new Intl.RelativeTimeFormat('en');
			const options = rtf.resolvedOptions();
			should(options).be.an.Object();
			should(options.locale).be.a.String();
			should(options.style).be.eql('long');
			should(options.numeric).be.eql('always');
		});

		it('returns custom options', () => {
			const rtf = new Intl.RelativeTimeFormat('en', { style: 'short', numeric: 'auto' });
			const options = rtf.resolvedOptions();
			should(options.style).be.eql('short');
			should(options.numeric).be.eql('auto');
		});
	});

	describe('style option', () => {
		it('short style produces shorter output', () => {
			const longRtf = new Intl.RelativeTimeFormat('en', { style: 'long', numeric: 'always' });
			const shortRtf = new Intl.RelativeTimeFormat('en', { style: 'short', numeric: 'always' });
			const longResult = longRtf.format(-3, 'month');
			const shortResult = shortRtf.format(-3, 'month');
			should(longResult).be.a.String();
			should(shortResult).be.a.String();
			// Short result should generally be equal or shorter in length
			should(shortResult.length).be.belowOrEqual(longResult.length);
		});

		it('narrow style', () => {
			const rtf = new Intl.RelativeTimeFormat('en', { style: 'narrow', numeric: 'always' });
			const result = rtf.format(-3, 'month');
			should(result).be.a.String();
			should(result.length).be.above(0);
		});
	});
});
