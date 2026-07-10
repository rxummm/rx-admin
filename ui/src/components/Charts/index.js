import BaseChart from './BaseChart.vue'
import BarChart from './BarChart.vue'
import LineChart from './LineChart.vue'
import PieChart from './PieChart.vue'

export {
  BaseChart,
  BarChart,
  LineChart,
  PieChart
}

export default {
  install(app) {
    app.component('BaseChart', BaseChart)
    app.component('BarChart', BarChart)
    app.component('LineChart', LineChart)
    app.component('PieChart', PieChart)
  }
}
