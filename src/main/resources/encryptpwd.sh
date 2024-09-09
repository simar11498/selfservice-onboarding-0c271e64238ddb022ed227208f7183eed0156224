#!/bin/sh
encrypt=`perl -e 'print unpack "H*","Teamw0rk"'`
echo $encrypt

decrypt=`perl -e 'print pack "H*","5465616d7730726b"'`
echo $decrypt