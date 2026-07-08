/*
 * Titanium SDK
 * Copyright TiDev, Inc. 04/07/2022-Present. All Rights Reserved.
 * Licensed under the terms of the Apache Public License
 * Please see the LICENSE included with this distribution for details.
 */
/* eslint-env mocha */
/* eslint no-unused-expressions: "off" */
/* eslint promise/no-callback-in-promise: "off" */
'use strict';
const should = require('./utilities/assertions');

describe('Titanium.UI.NavigationWindow', function () {
	this.timeout(10000);

	let nav;
	afterEach(done => {
		if (nav && !nav.closed) {
			nav.close().then(() => done()).catch(_e => done());
		} else {
			nav = null;
			done();
		}
	});

	it.iosBroken('namespace exists', () => { // should this be defined?
		should(Ti.UI.NavigationWindow).not.be.undefined();
	});

	describe('properties', () => {
		it('.apiName', () => {
			const view = Ti.UI.createNavigationWindow();
			should(view).have.readOnlyProperty('apiName').which.is.a.String();
			should(view.apiName).be.eql('Ti.UI.NavigationWindow');
		});

		describe('.windows', () => {
			it('is an Array', () => {
				const rootWindow = Ti.UI.createWindow();
				nav = Ti.UI.createNavigationWindow({ window: rootWindow });
				should(nav.windows).be.an.Array();
			});

			it('contains root window after open', function (finish) {
				const rootWindow = Ti.UI.createWindow();
				nav = Ti.UI.createNavigationWindow({ window: rootWindow });

				rootWindow.addEventListener('open', function open() {
					rootWindow.removeEventListener('open', open);
					try {
						should(nav.windows).be.an.Array();
						should(nav.windows.length).eql(1);
						should(nav.windows[0]).eql(rootWindow);
					} catch (err) {
						return finish(err);
					}
					finish();
				});

				nav.open();
			});

			it('updates when windows are opened and closed', function (finish) {
				const rootWindow = Ti.UI.createWindow();
				const subWindow = Ti.UI.createWindow();
				nav = Ti.UI.createNavigationWindow({ window: rootWindow });

				rootWindow.addEventListener('open', function open() {
					rootWindow.removeEventListener('open', open);
					try {
						should(nav.windows.length).eql(1);
					} catch (err) {
						return finish(err);
					}
					setTimeout(() => nav.openWindow(subWindow), 1);
				});

				subWindow.addEventListener('open', function open() {
					subWindow.removeEventListener('open', open);
					try {
						should(nav.windows.length).eql(2);
						should(nav.windows[0]).eql(rootWindow);
						should(nav.windows[1]).eql(subWindow);
					} catch (err) {
						return finish(err);
					}
					setTimeout(() => nav.closeWindow(subWindow), 1);
				});

				subWindow.addEventListener('close', function close() {
					subWindow.removeEventListener('close', close);
					try {
						should(nav.windows.length).eql(1);
						should(nav.windows[0]).eql(rootWindow);
					} catch (err) {
						return finish(err);
					}
					finish();
				});

				nav.open();
			});
		});
	});

	describe('methods', () => {
		describe('#close()', () => {
			it('is a Function', () => {
				const nav = Ti.UI.createNavigationWindow();

				should(nav).have.a.property('close').which.is.a.Function();
			});

			it('returns a Promise', finish => {
				nav = Ti.UI.createNavigationWindow({ window: Ti.UI.createWindow() });

				const openPromise = nav.open();
				openPromise.then(() => {
					const result = nav.close();
					result.should.be.a.Promise();
					return result.then(() => finish()).catch(e => finish(e)); // eslint-disable-line promise/no-nesting
				}).catch(e => finish(e));
			});

			it('called on unopened Window rejects Promise', finish => {
				nav = Ti.UI.createNavigationWindow({ window: Ti.UI.createWindow() });

				const result = nav.close();
				result.should.be.a.Promise();
				result.then(() => finish(new Error('Expected #close() to be rejected on unopened Window'))).catch(_e => finish());
			});

			it('called twice on Window rejects second Promise', finish => {
				nav = Ti.UI.createNavigationWindow({ window: Ti.UI.createWindow() });

				nav.open().then(() => {
					// eslint-disable-next-line promise/no-nesting
					return nav.close().then(() => {
						// eslint-disable-next-line promise/no-nesting
						return nav.close().then(() => finish(new Error('Expected second #close() call on Window to be rejected'))).catch(() => finish());
					}).catch(e => finish(e));
				}).catch(e => finish(e));
			});
		});

		it('#closeWindow()', () => {
			const nav = Ti.UI.createNavigationWindow();
			should(nav.closeWindow).be.a.Function();
		});

		describe('#open()', () => {
			it('is a Function', () => {
				nav = Ti.UI.createNavigationWindow();

				should(nav).have.a.property('open').which.is.a.Function();
			});

			it('returns a Promise', finish => {
				nav = Ti.UI.createNavigationWindow({ window: Ti.UI.createWindow() });

				const result = nav.open();
				result.should.be.a.Promise();
				result.then(() => finish()).catch(e => finish(e));
			});

			it('called twice on same Window rejects second Promise', finish => {
				nav = Ti.UI.createNavigationWindow({ window: Ti.UI.createWindow() });

				const first = nav.open();
				first.should.be.a.Promise();
				// eslint-disable-next-line promise/catch-or-return
				first.then(() => nav.open(), e => finish(e)).then(() => finish(new Error('Expected second #open() to be rejected')), _e => finish());
			});
		});

		describe('#openWindow()', () => {
			it('is a Function', () => {
				const view = Ti.UI.createNavigationWindow();
				should(view.openWindow).be.a.Function();
			});
		});

		// FIXME: Seems to be crashing silently on iOS?
		it('#openWindow, #closeWindow', function (finish) {
			const rootWindow = Ti.UI.createWindow();
			const subWindow = Ti.UI.createWindow();
			nav = Ti.UI.createNavigationWindow({
				window: rootWindow
			});

			rootWindow.addEventListener('open', () => {
				console.log('rootWindow open event');
				setTimeout(() => {
					try {
						nav.openWindow(subWindow);
						should(subWindow.navigationWindow).eql(nav);
					} catch (err) {
						finish(err);
					}
				}, 1);
			});

			subWindow.addEventListener('open', () => {
				console.log('subWindow open event');
				setTimeout(() => nav.closeWindow(subWindow), 1);
			});

			subWindow.addEventListener('close', () => {
				console.log('subWindow close event');
				try {
					should(subWindow.navigationWindow).not.be.ok(); // null or undefined
				} catch (err) {
					return finish(err);
				}
				finish();
			});

			nav.open();
		});

		describe('#popToRootWindow()', () => {
			it('is a Function', () => {
				const view = Ti.UI.createNavigationWindow();
				should(view.popToRootWindow).be.a.Function();
			});

			// FIXME: Crashes frequently on macOS on CI boxes
			it.macBroken('works without crashing', function (finish) {
				var rootWindow = Ti.UI.createWindow();
				var subWindow = Ti.UI.createWindow();

				nav = Ti.UI.createNavigationWindow({
					window: rootWindow
				});

				rootWindow.addEventListener('open', function open() {
					rootWindow.removeEventListener('open', open);
					setTimeout(() => nav.openWindow(subWindow), 1);
				});

				subWindow.addEventListener('close', function close () {
					subWindow.removeEventListener('close', close);
					try {
						should(subWindow.navigationWindow).not.be.ok(); // null or undefined
						// how else can we tell it got closed? I don't think a visible check is right...
						// win should not be closed!
						should(rootWindow.navigationWindow).eql(nav);
					} catch (err) {
						return finish(err);
					}
					finish();
				});

				subWindow.addEventListener('open', function open() {
					subWindow.removeEventListener('open', open);
					setTimeout(() => nav.popToRootWindow(), 1);
				});

				nav.open();
			});
		});

		describe('#insertWindow()', () => {
			it.ios('is a Function', () => {
				const view = Ti.UI.createNavigationWindow();
				should(view.insertWindow).be.a.Function();
			});

			it.ios('returns a Promise', function (finish) {
				const rootWindow = Ti.UI.createWindow({ title: 'Root' });
				const insertedWindow = Ti.UI.createWindow({ title: 'Inserted' });

				nav = Ti.UI.createNavigationWindow({
					window: rootWindow
				});

				rootWindow.addEventListener('open', function open() {
					rootWindow.removeEventListener('open', open);
					setTimeout(() => {
						const result = nav.insertWindow(insertedWindow, 1);
						try {
							result.should.be.a.Promise();
							result.then(() => finish()).catch(e => finish(e));
						} catch (err) {
							finish(err);
						}
					}, 1);
				});

				nav.open();
			});

			it.ios('inserts window at specified index', function (finish) {
				const rootWindow = Ti.UI.createWindow({ title: 'Root' });
				const insertedWindow = Ti.UI.createWindow({ title: 'Inserted' });
				const topWindow = Ti.UI.createWindow({ title: 'Top' });

				nav = Ti.UI.createNavigationWindow({
					window: rootWindow
				});

				rootWindow.addEventListener('open', function open() {
					rootWindow.removeEventListener('open', open);
					setTimeout(() => {
						// Open top window first
						nav.openWindow(topWindow, { animated: false });
					}, 1);
				});

				topWindow.addEventListener('open', function open() {
					topWindow.removeEventListener('open', open);
					setTimeout(() => {
						// Insert a window between root and top, then close top
						// Use Promise to ensure proper sequencing
						nav.insertWindow(insertedWindow, 1)
							.then(() => nav.closeWindow(topWindow, { animated: false }))
							.catch(e => finish(e));
					}, 1);
				});

				insertedWindow.addEventListener('focus', function focus() {
					insertedWindow.removeEventListener('focus', focus);
					try {
						// Verify the inserted window is now visible and in the stack
						should(insertedWindow.navigationWindow).eql(nav);
					} catch (err) {
						return finish(err);
					}
					finish();
				});

				nav.open();
			});

			it.ios('allows deep linking scenario - start at detail level with back navigation', function (finish) {
				const rootWindow = Ti.UI.createWindow({ title: 'Home' });
				const categoryWindow = Ti.UI.createWindow({ title: 'Category' });
				const detailWindow = Ti.UI.createWindow({ title: 'Detail' });

				nav = Ti.UI.createNavigationWindow({
					window: rootWindow
				});

				rootWindow.addEventListener('open', function open() {
					rootWindow.removeEventListener('open', open);
					setTimeout(() => {
						// Simulate deep link: insert category, then open detail
						// Use Promise to ensure proper sequencing
						nav.insertWindow(categoryWindow, 1)
							.then(() => nav.openWindow(detailWindow, { animated: false }))
							.catch(e => finish(e));
					}, 1);
				});

				detailWindow.addEventListener('open', function open() {
					detailWindow.removeEventListener('open', open);
					setTimeout(() => {
						try {
							// Verify all windows are in the navigation stack
							should(rootWindow.navigationWindow).eql(nav);
							should(categoryWindow.navigationWindow).eql(nav);
							should(detailWindow.navigationWindow).eql(nav);

							// Close detail window to test back navigation
							nav.closeWindow(detailWindow, { animated: false });
						} catch (err) {
							return finish(err);
						}
					}, 100);
				});

				categoryWindow.addEventListener('focus', function focus() {
					categoryWindow.removeEventListener('focus', focus);
					try {
						// Category window should now be visible after closing detail
						should(categoryWindow.navigationWindow).eql(nav);
					} catch (err) {
						return finish(err);
					}
					finish();
				});

				nav.open();
			});

			it.ios('insert then close works correctly with Promise', function (finish) {
				const rootWindow = Ti.UI.createWindow({ title: 'Root' });
				const currentWindow = Ti.UI.createWindow({ title: 'Current' });
				const insertedWindow = Ti.UI.createWindow({ title: 'Inserted' });

				nav = Ti.UI.createNavigationWindow({
					window: rootWindow
				});

				rootWindow.addEventListener('open', function open() {
					rootWindow.removeEventListener('open', open);
					setTimeout(() => nav.openWindow(currentWindow, { animated: false }), 1);
				});

				currentWindow.addEventListener('open', function open() {
					currentWindow.removeEventListener('open', open);
					setTimeout(() => {
						// This is the critical test: insert then close using Promise
						nav.insertWindow(insertedWindow, 1)
							.then(() => nav.closeWindow(currentWindow, { animated: false }))
							.catch(e => finish(e));
					}, 1);
				});

				insertedWindow.addEventListener('focus', function focus() {
					insertedWindow.removeEventListener('focus', focus);
					try {
						// Inserted window should now be visible
						should(insertedWindow.navigationWindow).eql(nav);
						// Current window should be closed
						should(currentWindow.navigationWindow).not.be.ok();
					} catch (err) {
						return finish(err);
					}
					finish();
				});

				nav.open();
			});
		});
	});

	it('open/close should open/close the window', function (finish) {
		var window = Ti.UI.createWindow(),
			navigation = Ti.UI.createNavigationWindow({
				window: window
			});

		window.addEventListener('open', function () {
			setTimeout(function () {
				navigation.close();
			}, 1);
		});
		window.addEventListener('close', function () {
			finish();
		});
		navigation.open();
	});

	it('open/close events', finish => {
		const window = Ti.UI.createWindow();

		nav = Ti.UI.createNavigationWindow({ window });

		nav.addEventListener('open', () => nav.close());
		nav.addEventListener('close', () => finish());

		nav.open();
	});

	it('basic open/close navigation', function (finish) {
		var rootWindow = Ti.UI.createWindow(),
			window2 = Ti.UI.createWindow(),
			navigation = Ti.UI.createNavigationWindow({
				window: rootWindow
			});

		rootWindow.addEventListener('open', function () {
			setTimeout(function () {
				navigation.openWindow(window2);
			}, 1);
		});
		window2.addEventListener('open', function () {
			setTimeout(function () {
				navigation.closeWindow(window2);
			}, 1);
		});
		rootWindow.addEventListener('close', function () {
			finish();
		});
		window2.addEventListener('close', function () {
			setTimeout(function () {
				navigation.close();
			}, 1);
		});
		navigation.open();
	});

	function createTab(title) {
		const window = Ti.UI.createWindow({ title });
		return Ti.UI.createTab({
			title,
			window
		});
	}

	it('have TabGroup as a root window', done => {
		const tabGroup = Ti.UI.createTabGroup({
			title: 'TabGroup',
			tabs: [
				createTab('Tab 1'),
				createTab('Tab 2'),
				createTab('Tab 3')
			]
		});
		nav = Ti.UI.createNavigationWindow({
			window: tabGroup
		});
		nav.open().then(() => done()).catch(e => done(e));
	});

	it('have a TabGroup child in stack', function () {
		var rootWin = Ti.UI.createWindow(),
			tabGroup = Ti.UI.createTabGroup({ title: 'TabGroup',
				tabs: [ createTab('Tab 1'),
					createTab('Tab 2'),
					createTab('Tab 3') ]
			});
		nav = Ti.UI.createNavigationWindow({
			window: rootWin
		});
		nav.open();
		nav.openWindow(tabGroup);
	});
});

describe('Titanium.UI.Window', function () {
	let nav;

	this.timeout(10000);

	afterEach(done => {
		if (nav) {
			nav.close().then(() => done()).catch(() => done());
		}
		nav = null;
	});

	it.windowsMissing('.navigationWindow', function (finish) {
		const rootWindow = Ti.UI.createWindow();
		nav = Ti.UI.createNavigationWindow({
			window: rootWindow
		});

		rootWindow.addEventListener('open', function () {
			try {
				should(nav).not.be.undefined();
				should(rootWindow.navigationWindow).eql(nav);
				should(rootWindow.navigationWindow.apiName).eql('Ti.UI.NavigationWindow');
			} catch (err) {
				return finish(err);
			}
			finish();
		});

		nav.open();
	});

	it('open window from open event of window (TIMOB-26838)', function (finish) {
		const window = Ti.UI.createWindow();
		console.log('created window, creating navigation window...');
		nav = Ti.UI.createNavigationWindow({ window	});
		console.log('created navigation window, creating next window...');

		const nextWindow = Ti.UI.createWindow();
		console.log('adding open listener to nextWindow...');
		nextWindow.addEventListener('open', () => {
			console.log('finished');
			finish();
		});
		console.log('adding open listener to first window...');
		window.addEventListener('open', () => {
			console.log('calling nav.openWindow()');
			nav.openWindow(nextWindow, { animated: true });
		});
		console.log('opening navigation window...');
		nav.open();
	});
});
