#! /usr/bin/env node
const { exec } = require("child_process");

function doExec(cmdString) {
  return new Promise((resolve, reject) => {
    exec(cmdString, (err, stdout) => {
      if (err) {
        reject(err);
        return;
      }
      resolve(stdout);
    });
  });
}

function run() {
  doExec(
    "grep -rl --include \\*.h --include \\*.cpp --include \\*.cc --include \\*.in AddLogSink ../node_modules/react-native/third-party/ | xargs sed -i '' 's/AddLogSink/ReactAddLogSink/g'"
  ).then(() =>
    doExec(
      "grep -rl --include \\*.h --include \\*.cpp --include \\*.cc --include \\*.in RemoveLogSink ../node_modules/react-native/third-party/ | xargs sed -i '' 's/RemoveLogSink/ReactRemoveLogSink/g'"
    )
  );
}

run();
