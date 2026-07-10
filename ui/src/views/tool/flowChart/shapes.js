// 自定义 X6 图形注册
// 单独抽离此文件，避免 Vue SFC 编译器对 refPoints 的解析歧义
import { Graph } from '@antv/x6'

export function registerCustomShapes() {
  // 使用 try-catch 防止 KeepAlive 复用导致重复注册
  try {
    var body = {}
    body.refPoints = '0,0.5 0.5,1 1,0.5 0.5,0'
    body.stroke = '#E6A23C'
    body.strokeWidth = 2
    body.fill = '#ffffff'

    Graph.registerNode('diamond-shape', {
      inherit: 'polygon',
      attrs: {
        body: body,
        label: { fontSize: 14, fill: '#303133' }
      },
      width: 100,
      height: 60
    })

    Graph.registerNode('circle-shape', {
      inherit: 'circle',
      attrs: {
        body: { stroke: '#67C23A', strokeWidth: 2, fill: '#ffffff' },
        label: { fontSize: 14, fill: '#303133' }
      },
      width: 70,
      height: 70
    })

    Graph.registerNode('ellipse-shape', {
      inherit: 'ellipse',
      attrs: {
        body: { stroke: '#8B5CF6', strokeWidth: 2, fill: '#ffffff' },
        label: { fontSize: 14, fill: '#303133' }
      },
      width: 120,
      height: 60
    })
  } catch {
    // shapes already registered, ignore
  }
}
