function escapeHtml(value: string) {
  return value.replace(/[&<>"']/g, char => ({ '&': '&amp;', '<': '&lt;', '>': '&gt;', '"': '&quot;', "'": '&#39;' })[char] || char)
}

/** Render common Markdown while escaping raw HTML from untrusted content. */
export function renderMarkdown(value?: string) {
  const inline = (source: string) => escapeHtml(source)
    .replace(/`([^`]+)`/g, '<code>$1</code>')
    .replace(/\*\*([^*]+)\*\*/g, '<strong>$1</strong>')
    .replace(/\*([^*]+)\*/g, '<em>$1</em>')
    .replace(/\[([^\]]+)\]\((https?:\/\/[^\s)]+)\)/g, '<a href="$2" target="_blank" rel="noopener noreferrer">$1</a>')
  const lines = (value || '').replace(/\r\n?/g, '\n').split('\n')
  const output: string[] = []
  let list: 'ul' | 'ol' | '' = ''
  let code = false
  const closeList = () => { if (list) output.push(`</${list}>`); list = '' }

  for (const line of lines) {
    if (line.startsWith('```')) { closeList(); output.push(code ? '</code></pre>' : '<pre><code>'); code = !code; continue }
    if (code) { output.push(`${escapeHtml(line)}\n`); continue }
    const heading = line.match(/^(#{1,4})\s+(.+)$/)
    const unordered = line.match(/^[-*+]\s+(.+)$/)
    const ordered = line.match(/^\d+\.\s+(.+)$/)
    if (heading) { closeList(); const level = heading[1].length; output.push(`<h${level}>${inline(heading[2])}</h${level}>`); continue }
    if (unordered || ordered) {
      const next = unordered ? 'ul' : 'ol'
      if (list && list !== next) closeList()
      if (!list) { list = next; output.push(`<${list}>`) }
      output.push(`<li>${inline((unordered || ordered)![1])}</li>`)
      continue
    }
    closeList()
    if (!line.trim()) continue
    if (line.startsWith('> ')) output.push(`<blockquote>${inline(line.slice(2))}</blockquote>`)
    else if (/^---+$/.test(line)) output.push('<hr>')
    else output.push(`<p>${inline(line)}</p>`)
  }
  closeList()
  if (code) output.push('</code></pre>')
  return output.join('')
}
