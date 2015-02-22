#!/usr/bin/perl
for $i (1 .. 10) {
   system("lein with-profile test do cljsbuild test");
}