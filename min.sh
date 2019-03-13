#!/usr/bin/env bash

lein clean
lein run -m figwheel.main --build-once min
