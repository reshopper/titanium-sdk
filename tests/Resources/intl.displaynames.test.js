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

describe('Intl.DisplayNames', () => {
	it('exists as a constructor', () => {
		should(Intl.DisplayNames).not.be.undefined();
		should(Intl.DisplayNames).be.a.Function();
	});

	describe('#supportedLocalesOf()', () => {
		it('is a Function', () => {
			should(Intl.DisplayNames.supportedLocalesOf).not.be.undefined();
			should(Intl.DisplayNames.supportedLocalesOf).be.a.Function();
		});

		it('returns array for valid locale', () => {
			const locales = Intl.DisplayNames.supportedLocalesOf('en-US');
			should(locales).be.an.Array();
			should(locales.length).be.aboveOrEqual(1);
		});

		it('returns empty array for invalid locale', () => {
			const locales = Intl.DisplayNames.supportedLocalesOf('xx-XX');
			should(locales).be.an.Array();
			should(locales.length).be.eql(0);
		});
	});

	describe('type: language', () => {
		it('returns display name for language code', () => {
			const dn = new Intl.DisplayNames([ 'en' ], { type: 'language' });
			const result = dn.of('fr');
			should(result).be.a.String();
			should(result.length).be.above(0);
			// Should return "French" in English locale
			should(result).be.eql('French');
		});

		it('returns localized display name', () => {
			const dn = new Intl.DisplayNames([ 'de' ], { type: 'language' });
			const result = dn.of('fr');
			should(result).be.a.String();
			should(result.length).be.above(0);
			// Should return "Französisch" in German locale
			should(result).be.eql('Französisch');
		});
	});

	describe('type: region', () => {
		it('returns display name for region code', () => {
			const dn = new Intl.DisplayNames([ 'en' ], { type: 'region' });
			const result = dn.of('US');
			should(result).be.a.String();
			should(result.length).be.above(0);
			should(result).be.eql('United States');
		});

		it('returns localized display name', () => {
			const dn = new Intl.DisplayNames([ 'de' ], { type: 'region' });
			const result = dn.of('US');
			should(result).be.a.String();
			should(result.length).be.above(0);
			should(result).be.eql('Vereinigte Staaten');
		});
	});

	describe('type: script', () => {
		it('returns display name for script code', () => {
			const dn = new Intl.DisplayNames([ 'en' ], { type: 'script' });
			const result = dn.of('Latn');
			should(result).be.a.String();
			should(result.length).be.above(0);
			should(result).be.eql('Latin');
		});
	});

	describe('type: currency', () => {
		it('returns display name for currency code', () => {
			const dn = new Intl.DisplayNames([ 'en' ], { type: 'currency' });
			const result = dn.of('USD');
			should(result).be.a.String();
			should(result.length).be.above(0);
			should(result).be.eql('US Dollar');
		});

		it('returns localized display name for currency', () => {
			const dn = new Intl.DisplayNames([ 'de' ], { type: 'currency' });
			const result = dn.of('USD');
			should(result).be.a.String();
			should(result.length).be.above(0);
			should(result).be.eql('US-Dollar');
		});
	});

	describe('#resolvedOptions()', () => {
		it('returns expected options', () => {
			const dn = new Intl.DisplayNames([ 'en' ], { type: 'language' });
			const options = dn.resolvedOptions();
			should(options).be.an.Object();
			should(options.locale).be.a.String();
			should(options.type).be.eql('language');
			should(options.style).be.eql('long');
			should(options.fallback).be.eql('code');
		});

		it('includes languageDisplay for language type', () => {
			const dn = new Intl.DisplayNames([ 'en' ], { type: 'language', languageDisplay: 'standard' });
			const options = dn.resolvedOptions();
			should(options.languageDisplay).be.eql('standard');
		});
	});

	describe('fallback behavior', () => {
		it('fallback "code" returns code for unknown', () => {
			const dn = new Intl.DisplayNames([ 'en' ], { type: 'region', fallback: 'code' });
			const result = dn.of('ZZ');
			should(result).be.eql('ZZ');
		});

		it('fallback "none" returns null/undefined for unknown', () => {
			const dn = new Intl.DisplayNames([ 'en' ], { type: 'region', fallback: 'none' });
			const result = dn.of('ZZ');
			// Should return undefined or null for unknown codes with "none" fallback
			should(result === null || result === undefined).be.true();
		});
	});
});
