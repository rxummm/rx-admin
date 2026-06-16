function getCSSColor(name, fallback) {
  if (typeof getComputedStyle === 'undefined') return fallback
  const val = getComputedStyle(document.documentElement).getPropertyValue(name).trim()
  return val || fallback
}

export function createCyberTheme(primaryColor) {
  const pc = primaryColor || getCSSColor('--color-primary', '#58a6ff')

  return {
    color: [
      pc,
      '#3fb950',
      '#d29922',
      '#f85149',
      '#56d4dd',
      '#db61a2',
      '#a371f7',
      '#8b949e'
    ],

    backgroundColor: 'transparent',

    textStyle: {
      color: '#8b949e',
      fontFamily: "'Inter', 'IBM Plex Sans', sans-serif"
    },

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

    legend: {
      textStyle: {
        color: '#8b949e',
        fontSize: 12
      },
      itemWidth: 12,
      itemHeight: 12,
      itemGap: 16
    },

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
          color: pc,
          width: 1,
          type: 'dashed'
        },
        label: {
          backgroundColor: pc,
          color: '#ffffff',
          borderRadius: 4,
          padding: [4, 8]
        }
      }
    },

    grid: {
      top: 40,
      right: 20,
      bottom: 30,
      left: 50,
      containLabel: true
    },

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

    series: {
      line: {
        symbol: 'circle',
        symbolSize: 6,
        smooth: true,
        lineStyle: {
          width: 2,
          shadowColor: pc + '4d',
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

      scatter: {
        symbolSize: 10,
        itemStyle: {
          shadowColor: pc + '80',
          shadowBlur: 8
        }
      },

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

    dataZoom: {
      backgroundColor: '#161b22',
      fillerColor: pc + '33',
      borderColor: '#30363d',
      handleStyle: {
        color: pc,
        borderColor: pc
      },
      textStyle: {
        color: '#8b949e'
      }
    },

    timeline: {
      lineStyle: {
        color: '#30363d'
      },
      controlStyle: {
        color: pc,
        borderColor: pc
      },
      itemStyle: {
        color: pc,
        borderWidth: 2
      },
      label: {
        color: '#8b949e'
      }
    }
  }
}

let _cachedTheme = null

export function getCyberTheme() {
  if (!_cachedTheme) {
    _cachedTheme = createCyberTheme()
  }
  return _cachedTheme
}

export function invalidateCyberTheme() {
  _cachedTheme = null
}

export function registerCyberTheme(echarts) {
  echarts.registerTheme('cyberpunk', createCyberTheme())
}
