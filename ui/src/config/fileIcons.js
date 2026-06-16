const FILE_EXT_COLORS = {
  pdf: '#F56C6C',
  doc: '#409EFF',
  docx: '#409EFF',
  xls: '#67C23A',
  xlsx: '#67C23A',
  ppt: '#E6A23C',
  pptx: '#E6A23C',
  zip: '#909399',
  rar: '#909399',
  '7z': '#909399',
  jpg: '#67C23A',
  jpeg: '#67C23A',
  png: '#67C23A',
  gif: '#67C23A',
  txt: '#909399',
  csv: '#67C23A',
}

export function getFileIconColor(fileName) {
  if (!fileName) return '#909399'
  const ext = fileName.split('.').pop()?.toLowerCase()
  return FILE_EXT_COLORS[ext] || '#909399'
}
