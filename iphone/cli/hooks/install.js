/*
 * install.js: Titanium iOS CLI install hook
 *
 * Copyright TiDev, Inc. 04/07/2022-Present. All Rights Reserved.
 * See the LICENSE file for more information.
 */

import appc from 'node-appc';
import async from 'async';
import ioslib from 'ioslib';

export const cliVersion = '>=3.2';

export function init(logger, config, cli) {
	cli.addHook('build.post.compile', {
		priority: 8000,
		post: function (builder, finished) {
			if (cli.argv.target !== 'device') {
				return finished();
			}

			if (cli.argv['build-only']) {
				logger.info('Performed build only, skipping installing of the application');
				return finished();
			}

			ioslib.device.detect({ bypassCache: true }, function (err, results) {
				const devices = {};
				if (!err) {
					results.devices.forEach(function (device) {
						if (device.udid !== 'all' && (builder.deviceId === 'all' || device.udid === builder.deviceId)) {
							devices[device.udid] = device;
						}
					});
				}

				const udids = Object.keys(devices),
					levels = logger.getLevels(),
					logLevelRE = new RegExp('^(\u001b\\[\\d+m)?\\[?(' + levels.join('|') + '|log|timestamp)\\]?\\s*(\u001b\\[\\d+m)?(.*)', 'i'), // eslint-disable-line security/detect-non-literal-regexp
					// NSLog writes "<date> <time> <process>[<pid>:<tid>] " ahead of each line on stderr
					nsLogPrefixRE = /^\d{4}-\d{2}-\d{2} \d{2}:\d{2}:\d{2}\.\d+[+-]?\d* \S+\[\d+:[0-9a-f]+\] /i,
					handles = {};
				let startLog = false,
					runningCount = 0;

				function quit(force, udid) {
					runningCount--;

					if (udid && handles[udid]) {
						handles[udid].stop();
						delete handles[udid];
					} else if (force) {
						Object.keys(handles).forEach(function (udid) {
							handles[udid].stop();
							delete handles[udid];
						});
					}

					if (force || runningCount <= 0) {
						if (startLog) {
							const endLogTxt = 'End application log';
							logger.log(('-- ' + endLogTxt + ' ' + (new Array(75 - endLogTxt.length)).join('-')).grey + '\n');
						}
						process.exit(0);
					}
				}

				function relayLog(msg, udid) {
					if (!startLog) {
						const startLogTxt = 'Start application log';
						logger.log(('-- ' + startLogTxt + ' ' + (new Array(75 - startLogTxt.length)).join('-')).grey);
						startLog = true;
					}

					msg = msg.replace(nsLogPrefixRE, '');
					let m = msg.match(logLevelRE);
					if (m) {
						let line = m[0].trim();
						m = line.match(logLevelRE);
						if (m) {
							handles[udid].lastLogger = m[2].toLowerCase();
							line = m[4].trim();
						}
						msg = line;
					}

					const lastLogger = handles[udid].lastLogger;
					if (levels.indexOf(lastLogger) === -1) {
						logger.log(('[' + lastLogger.toUpperCase() + '] ').cyan + msg);
					} else {
						logger[lastLogger](msg);
					}
				}

				function launch(udid, device) {
					const handle = handles[udid] = ioslib.device
						.launch(udid, builder.tiapp.id)
						.on('locked', function () {
							logger.warn(`Device ${device.name.cyan} is locked, unlock it to launch the app`);
						})
						.on('app-started', function () {
							runningCount++;
						})
						.on('log', function (msg) {
							relayLog(msg, udid);
						})
						.on('app-quit', function () { quit(false, udid); })
						.on('error', function (err) {
							logger.error(`Failed to launch app on device ${device.name.cyan}: ${err.message || err}`);
							logger.log('Please manually launch the application'.magenta + '\n');
						});
					handle.lastLogger = 'debug';
				}

				// install the app for the specified device or "all" devices
				async.eachSeries(udids, function (udid, next) {
					const device = devices[udid];

					logger.info(`Installing app on device: ${device.name.cyan}`);

					ioslib.device
						.install(udid, builder.xcodeAppDir)
						.on('installed', function () {
							logger.info(`App successfully installed on device: ${device.name.cyan}`);
							cli.emit('build.post.install', builder, function (err) {
								if (!err) {
									launch(udid, device);
								}
								next(err);
							});
						})
						.on('error', function (err) {
							next(new appc.exception(err.message || err.toString(), 'The app failed to install on the device. Make sure the device is unlocked, paired with this Mac and registered in your provisioning profile, and that Xcode 15 or newer is selected.'));
						});
				}, finished);

				// listen for ctrl-c
				process.on('SIGINT', function () {
					logger.log();
					quit(true);
				});
			});
		}
	});
}
