/**
 * Array polyfills for ES2022+ features not available in older JS engines.
 *
 * Required because:
 * - Android uses V8 8.8 (Chrome 88), but .at() was added in V8 9.0 (Chrome 89)
 * - iOS minimum is 15.0, but .at() was added in Safari 15.4 (iOS 15.4)
 */
/* global BigInt64Array, BigUint64Array */

// Array.prototype.at() - ES2022
// Allows relative indexing with negative indices
if (!Array.prototype.at) {
	Object.defineProperty(Array.prototype, 'at', {
		value: function (index) {
			const length = this.length;
			const relativeIndex = Math.trunc(index) || 0;
			const actualIndex = relativeIndex < 0 ? length + relativeIndex : relativeIndex;
			if (actualIndex < 0 || actualIndex >= length) {
				return undefined;
			}
			return this[actualIndex];
		},
		writable: true,
		enumerable: false,
		configurable: true
	});
}

// String.prototype.at() - ES2022
// Same feature for strings
if (!String.prototype.at) {
	Object.defineProperty(String.prototype, 'at', {
		value: function (index) {
			const length = this.length;
			const relativeIndex = Math.trunc(index) || 0;
			const actualIndex = relativeIndex < 0 ? length + relativeIndex : relativeIndex;
			if (actualIndex < 0 || actualIndex >= length) {
				return undefined;
			}
			return this.charAt(actualIndex);
		},
		writable: true,
		enumerable: false,
		configurable: true
	});
}

// TypedArray.prototype.at() - ES2022
// Apply to all TypedArray types
const typedArrayTypes = [
	Int8Array,
	Uint8Array,
	Uint8ClampedArray,
	Int16Array,
	Uint16Array,
	Int32Array,
	Uint32Array,
	Float32Array,
	Float64Array
];

// Check if BigInt typed arrays exist (ES2020)
if (typeof BigInt64Array !== 'undefined') {
	typedArrayTypes.push(BigInt64Array);
}
if (typeof BigUint64Array !== 'undefined') {
	typedArrayTypes.push(BigUint64Array);
}

for (const TypedArray of typedArrayTypes) {
	if (!TypedArray.prototype.at) {
		Object.defineProperty(TypedArray.prototype, 'at', {
			value: function (index) {
				const length = this.length;
				const relativeIndex = Math.trunc(index) || 0;
				const actualIndex = relativeIndex < 0 ? length + relativeIndex : relativeIndex;
				if (actualIndex < 0 || actualIndex >= length) {
					return undefined;
				}
				return this[actualIndex];
			},
			writable: true,
			enumerable: false,
			configurable: true
		});
	}
}
