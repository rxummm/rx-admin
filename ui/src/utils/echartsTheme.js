/**
 * ECharts 赛博朋克主题配置
 * 参考 GitHub Dark / VS Code 配色方案
 */

export const cyberTheme = {
  // 全局颜色调色板
  color: [
    '#58a6ff', // 电光蓝
    '#3fb950', // 荧光绿
    '#d29922', // 琥珀黄
    '#f85149', // 珊瑚红
    '#56d4dd', // 青蓝
    '#db61a2', // 粉红
    '#a371f7', // 紫色
    '#8b949e'  // 中灰
  ],

  // 背景色（透明，继承容器背景）
  backgroundColor: 'transparent',

  // 全局文本样式
  textStyle: {
    color: '#8b949e',
    fontFamily: "'Inter', 'IBM Plex Sans', sans-serif"
  },

  // 标题样式
  title: {
    textStyle: {
      color: '#e6edf3',
      fontSize: 16,
      fontWeight: 600
    },
    subtextStyle: {
      color: '#8b949e',
      fontSize: 12
    }
  },

  // 图例样式
  legend: {
    textStyle: {
      color: '#8b949e',
      fontSize: 12
    },
    itemWidth: 12,
    itemHeight: 12,
    itemGap: 16
  },

  // 提示框样式
  tooltip: {
    backgroundColor: 'rgba(22, 27, 34, 0.95)',
    borderColor: '#30363d',
    borderWidth: 1,
    padding: [10, 14],
    textStyle: {
      color: '#e6edf3',
      fontSize: 13
    },
    axisPointer: {
      lineStyle: {
        color: '#58a6ff',
        width: 1,
        type: 'dashed'
      },
      label: {
        backgroundColor: '#58a6ff',
        color: '#ffffff',
        borderRadius: 4,
        padding: [4, 8]
      }
    }
  },

  // 网格线样式
  grid: {
    top: 40,
    right: 20,
    bottom: 30,
    left: 50,
    containLabel: true
  },

  // X/Y 轴通用配置
  xAxis: {
    axisLine: {
      lineStyle: {
        color: '#30363d'
      }
    },
    axisTick: {
      lineStyle: {
        color: '#30363d'
      }
    },
    axisLabel: {
      color: '#8b949e',
      fontSize: 12,
      margin: 12
    },
    splitLine: {
      lineStyle: {
        color: '#21262d',
        type: 'dashed'
      }
    }
  },

  yAxis: {
    axisLine: {
      lineStyle: {
        color: '#30363d'
      }
    },
    axisTick: {
      lineStyle: {
        color: '#30363d'
      }
    },
    axisLabel: {
      color: '#8b949e',
      fontSize: 12,
      margin: 12
    },
    splitLine: {
      lineStyle: {
        color: '#21262d',
        type: 'dashed'
      }
    }
  },

  // 系列默认配置
  series: {
    // 折线图
    line: {
      symbol: 'circle',
      symbolSize: 6,
      smooth: true,
      lineStyle: {
        width: 2,
        shadowColor: 'rgba(88, 166, 255, 0.3)',
        shadowBlur: 8,
        shadowOffsetY: 4
      },
      itemStyle: {
        borderWidth: 2,
        borderColor: '#0d1117'
      },
      areaStyle: {
        opacity: 0.1
      }
    },

    // 柱状图
    bar: {
      barMaxWidth: 40,
      itemStyle: {
        borderRadius: [4, 4, 0, 0],
        borderColor: '#0d1117',
        borderWidth: 2,
        shadowColor: 'rgba(0, 0, 0, 0.3)',
        shadowBlur: 4,
        shadowOffsetY: 2
      }
    },

    // 饼图
    pie: {
      itemStyle: {
        borderRadius: 6,
        borderColor: '#0d1117',
        borderWidth: 2
      },
      label: {
        color: '#e6edf3',
        fontSize: 13
      },
      labelLine: {
        lineStyle: {
          color: '#30363d'
        }
      }
    },

    // 散点图
    scatter: {
      symbolSize: 10,
      itemStyle: {
        shadowColor: 'rgba(88, 166, 255, 0.5)',
        shadowBlur: 8
      }
    },

    // 雷达图
    radar: {
      axisLine: {
        lineStyle: {
          color: '#30363d'
        }
      },
      splitLine: {
        lineStyle: {
          color: '#21262d'
        }
      },
      splitArea: {
        areaStyle: {
          color: ['#161b22', '#0d1117']
        }
      }
    }
  },

  // 数据区域缩放组件
  dataZoom: {
    backgroundColor: '#161b22',
    fillerColor: 'rgba(88, 166, 255, 0.2)',
    borderColor: '#30363d',
    handleStyle: {
      color: '#58a6ff',
      borderColor: '#58a6ff'
    },
    textStyle: {
      color: '#8b949e'
    }
  },

  // 时间轴组件
  timeline: {
    lineStyle: {
      color: '#30363d'
    },
    controlStyle: {
      color: '#58a6ff',
      borderColor: '#58a6ff'
    },
    itemStyle: {
      color: '#58a6ff',
      borderWidth: 2
    },
    label: {
      color: '#8b949e'
    }
  }
}

// 暗色主题专属增强
export const darkThemeEnhance = {
  series: {
    line: {
      lineStyle: {
        shadowColor: 'rgba(88, 166, 255, 0.5)'
      }
    },
    bar: {
      itemStyle: {
        shadowColor: 'rgba(0, 0, 0, 0.5)'
      }
    }
  }
}

// 导出主题注册函数
export function registerCyberTheme(echarts) {
  echarts.registerTheme('cyberpunk', cyberTheme)
}
