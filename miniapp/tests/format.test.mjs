import assert from 'node:assert/strict'
import { readFile } from 'node:fs/promises'

const source = await readFile(new URL('../utils/format.js', import.meta.url), 'utf8')
const { formatDateTime, summary } = await import(`data:text/javascript,${encodeURIComponent(source)}`)

assert.equal(formatDateTime('2026-07-17T10:30:00'), '2026-07-17 10:30')
assert.equal(summary('<p>校园 <strong>新闻</strong></p>'), '校园 新闻')
