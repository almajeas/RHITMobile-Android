#!/bin/bash

cd BetaManager
ant release install
cd ..

cd MobileDirectory
ant release install
cd ..
