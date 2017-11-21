#!/bin/bash
grep -rl --include \*.h --include \*.cpp --include \*.cc --include \*.in AddLogSink ./node_modules/react-native/third-party/ | xargs sed -i '' 's/AddLogSink/ReactAddLogSink/g'
grep -rl --include \*.h --include \*.cpp --include \*.cc --include \*.in RemoveLogSink ./node_modules/react-native/third-party/ | xargs sed -i '' 's/RemoveLogSink/ReactRemoveLogSink/g'