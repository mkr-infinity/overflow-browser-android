# Overflow Browser — Performance Guide

## Memory Optimization
- Reuses WebView instances across tab reloads where possible.
- Cleans up DOM storage and memory caches on low-memory callbacks.
- Closes idle incognito WebView sessions immediately to release native WebKit heap.

## Rendering Optimization
- Hardware acceleration enabled for smooth 60/120fps scrolling.
- Jetpack Compose lazy lists with stable keys for bookmark and history rendering.
- Vector drawables used exclusively to eliminate bitmap scaling overhead.
