var currentYear = new Date().getFullYear();

let chartInstance = null;
let monthlyChartInstance = null;
let lastData = null;

// =============================================
// THEME TOGGLE
// =============================================
const html = document.documentElement;
const toggleBtn = document.getElementById('theme-toggle');

// On first load: respect OS preference, then remember user choice
const saved = localStorage.getItem('theme');
if (saved) {
  html.setAttribute('data-theme', saved);
} else {
  const prefersDark = window.matchMedia('(prefers-color-scheme: dark)').matches;
  html.setAttribute('data-theme', prefersDark ? 'dark' : 'light');
}

toggleBtn.addEventListener('click', () => {
  const next = html.getAttribute('data-theme') === 'dark' ? 'light' : 'dark';
  html.setAttribute('data-theme', next);
  localStorage.setItem('theme', next);
  if (chartInstance) {
    updateChartTheme();
  }
});

// Read live CSS variables so chart colours always match the theme
function getThemeColors() {
  const s = getComputedStyle(html);
  return {
    border: s.getPropertyValue('--chart-border').trim(),
    tooltip: s.getPropertyValue('--tooltip-bg').trim(),
    tooltipBorder: s.getPropertyValue('--tooltip-border').trim(),
    muted: s.getPropertyValue('--muted').trim(),
    text: s.getPropertyValue('--text').trim(),
  };
}

function updateChartTheme() {
  const c = getThemeColors();
  chartInstance.data.datasets[0].borderColor = c.border;
  chartInstance.options.plugins.legend.labels.color = c.muted;
  chartInstance.options.plugins.tooltip.backgroundColor = c.tooltip;
  chartInstance.options.plugins.tooltip.borderColor = c.tooltipBorder;
  chartInstance.options.plugins.tooltip.titleColor = c.text;
  chartInstance.update();
  if (monthlyChartInstance && lastData) {
    renderBreakdownChart(lastData);
  }
}

const MONTH_LABELS = ['Jan', 'Feb', 'Mar', 'Apr', 'May', 'Jun',
  'Jul', 'Aug', 'Sep', 'Oct', 'Nov', 'Dec'];

function renderBreakdownChart(data) {
  if (monthlyChartInstance) { monthlyChartInstance.destroy(); monthlyChartInstance = null; }

  const isAllTime = !/^\d{4}$/.test(data.period);
  document.getElementById('breakdown-title').textContent = isAllTime ? 'Yearly Breakdown' : 'Monthly Breakdown';
  document.getElementById('monthly-year-badge').textContent = data.period;

  const totalsMap = data.periodTotals;
  const labels = isAllTime
      ? Object.keys(totalsMap).map(Number).sort((a, b) => a - b).map(String)
      : MONTH_LABELS;
  const values = isAllTime
      ? labels.map(y => { const v = totalsMap[Number(y)]; return v != null ? Number(v) : 0; })
      : MONTH_LABELS.map((_, i) => { const v = totalsMap[i + 1]; return v != null ? Number(v) : 0; });

  const s = getComputedStyle(document.documentElement);
  const accentColor  = s.getPropertyValue('--accent').trim();
  const accent2Color = s.getPropertyValue('--accent2').trim();
  const tealColor    = s.getPropertyValue('--teal').trim();
  const borderColor  = s.getPropertyValue('--border').trim();
  const c = {
    tooltip:       s.getPropertyValue('--tooltip-bg').trim(),
    tooltipBorder: s.getPropertyValue('--tooltip-border').trim(),
    text:          s.getPropertyValue('--text').trim(),
    muted:         s.getPropertyValue('--muted').trim(),
  };

  const max = Math.max(...values, 1);
  const barColors = values.map(v => {
    const r = v / max;
    if (r > 0.66) return accentColor;
    if (r > 0.33) return accent2Color;
    return tealColor;
  });

  monthlyChartInstance = new Chart(
    document.getElementById('monthly-chart').getContext('2d'), {
      type: 'bar',
      data: {
        labels: labels,
        datasets: [{ data: values, backgroundColor: barColors, borderRadius: 6, borderSkipped: false }]
      },
      options: {
        responsive: true,
        maintainAspectRatio: false,
        plugins: {
          legend: { display: false },
          tooltip: {
            backgroundColor: c.tooltip, borderColor: c.tooltipBorder, borderWidth: 1,
            titleColor: c.text, bodyColor: c.muted,
            titleFont: { family: "'Nunito', sans-serif", weight: 'bold' },
            bodyFont: { family: "'Rubik', sans-serif", size: 13 },
            callbacks: { label: ctx => ` ${fmt(ctx.parsed.y)}` }
          }
        },
        scales: {
          x: {
            grid: { color: borderColor },
            ticks: { color: c.muted, font: { family: "'Nunito', sans-serif", weight: '700', size: 11 } }
          },
          y: {
            grid: { color: borderColor },
            ticks: { color: c.muted, font: { family: "'Rubik', sans-serif", size: 11 }, callback: v => fmt(v) },
            beginAtZero: true
          }
        }
      }
    }
  );
}

// =============================================
// AUTH CHECK
// =============================================
function populateYearSelect(startYear) {
  var sel = document.getElementById('year-select');
  var allOpt = document.createElement('option');
  allOpt.value = 'all';
  allOpt.textContent = startYear + ' – now';
  sel.appendChild(allOpt);
  for (var y = currentYear; y >= startYear; y--) {
    var opt = document.createElement('option');
    opt.value = y;
    opt.textContent = y;
    if (y === currentYear) { opt.selected = true; }
    sel.appendChild(opt);
  }
}

function setAppName(name) {
  document.title = name;
  const h1 = document.querySelector('.logo-block h1');
  const parts = name.match(/^([A-Z][a-z]+)([A-Z].+)$/);
  if (parts) {
    h1.innerHTML = parts[1] + '<span>' + parts[2] + '</span>';
  } else {
    h1.textContent = name;
  }
}

async function init() {
  try {
    const [configRes, meRes] = await Promise.all([fetch('/api/config'), fetch('/api/me')]);
    const config = configRes.ok ? await configRes.json() : { name: 'GmailScanner', startYear: 2020 };
    setAppName(config.name || 'GmailScanner');
    populateYearSelect(config.startYear);
    if (meRes.ok) {
      showScanSection();
    } else {
      showAuthSection();
    }
  } catch {
    setAppName('GmailScanner');
    populateYearSelect(2020);
    showScanSection(); // fallback: Spring Security handles it
  }
}

function showAuthSection() {
  document.getElementById('auth-section').style.display = 'block';
  document.getElementById('scan-section').style.display = 'none';
}

function showScanSection() {
  document.getElementById('auth-section').style.display = 'none';
  document.getElementById('scan-section').style.display = 'block';
}

// =============================================
// SCAN
// =============================================
async function runScan() {
  const group = document.getElementById('group-select').value;
  const year = document.getElementById('year-select').value;
  const btn = document.getElementById('scan-btn');
  const errEl = document.getElementById('error-msg');

  errEl.style.display = 'none';
  btn.textContent = 'Scanning…';
  btn.classList.add('loading');
  btn.disabled = true;

  try {
    const res = await fetch(`/scan/${group}/${year}`);
    if (!res.ok) {
      if (res.status === 401 || res.status === 403) {
        const body = await res.json().catch(() => ({}));
        if (body.error === 'insufficient_scope') {
          // Redirect to OAuth with prompt=consent to force scope re-grant
          window.location.href = '/oauth2/authorization/google?prompt=consent';
        } else {
          showAuthSection();
        }
        return;
      }
      throw new Error(`Server error: ${res.status}`);
    }
    renderResults(await res.json());
  } catch (e) {
    errEl.textContent = e.message || 'Something went wrong. Try again.';
    errEl.style.display = 'block';
    document.getElementById('results').style.display = 'none';
    document.getElementById('empty').style.display = 'block';
  } finally {
    btn.textContent = 'Run Scan';
    btn.classList.remove('loading');
    btn.disabled = false;
  }
}

const fmt = n =>
    new Intl.NumberFormat('en-US',
        {style: 'currency', currency: 'EUR', maximumFractionDigits: 2}).format(n);

// Shrink font until text fits its container
function fitText(el, maxPx = 35) {
  el.style.fontSize = maxPx + 'px';
  while (el.scrollWidth > el.parentElement.clientWidth - 36 && maxPx > 10) {
    el.style.fontSize = --maxPx + 'px';
  }
}

// =============================================
// RENDER
// =============================================
function renderResults(data) {
  document.getElementById('empty').style.display = 'none';
  document.getElementById('results').style.display = 'block';

  const totalOrders = data.sources.reduce((s, r) => s + r.orders, 0);

  // Stat cards — data-emoji drives the watermark via CSS ::after
  document.getElementById('stats-row').innerHTML = `
    <div class="stat-card" data-emoji="💸">
      <div class="stat-label">Total Spent</div>
      <div class="stat-value">${fmt(data.totalSum)}</div>
    </div>
    <div class="stat-card" data-emoji="📅">
      <div class="stat-label">Monthly Avg</div>
      <div class="stat-value">${fmt(data.avgMonth)}</div>
    </div>
    <div class="stat-card" data-emoji="🛒">
      <div class="stat-label">Total Orders</div>
      <div class="stat-value">${totalOrders}</div>
    </div>
    <div class="stat-card" data-emoji="📆">
      <div class="stat-label">Period</div>
      <div class="stat-value stat-value--fit">${data.group.toUpperCase()} ${data.period}</div>
    </div>
  `;

  document.getElementById('result-text').textContent =
      data.msg || `You spent ${fmt(data.totalSum)} on ${data.group} in ${data.period}.`;

  requestAnimationFrame(() => {
    const el = document.querySelector('.stat-value--fit');
    if (el) {
      fitText(el);
    }
  });

  // Sources table
  const maxSum = Math.max(...data.sources.map(s => s.sum));
  document.getElementById('sources-tbody').innerHTML = data.sources
  .sort((a, b) => b.sum - a.sum)
  .map(s => {
    const pct = maxSum > 0 ? (s.sum / maxSum * 100).toFixed(1) : 0;
    const name = (s.source?.name || s.source || 'Unknown').toLowerCase().replace(/_/g, ' ');
    return `
        <tr>
          <td><span class="source-name">${name}</span></td>
          <td>
            <div class="bar-wrap">
              <div class="bar-track"><div class="bar-fill" style="width:${pct}%"></div></div>
              <span class="orders-badge">${s.orders}x</span>
            </div>
          </td>
          <td class="sum-cell">${fmt(s.sum)}</td>
        </tr>`;
  }).join('');

  document.getElementById('source-count').textContent =
      `${data.sources.length} source${data.sources.length !== 1 ? 's' : ''}`;

  renderChart(data.sources);
  lastData = data;
  renderBreakdownChart(data);
}

function renderChart(sources) {
  if (chartInstance) {
    chartInstance.destroy();
    chartInstance = null;
  }

  const sorted = [...sources].sort((a, b) => b.sum - a.sum);
  const labels = sorted.map(
      s => (s.source?.name || s.source || 'Unknown').toLowerCase().replace(/_/g, ' '));
  const values = sorted.map(s => s.sum);
  const palette = ['#38bdf8', '#818cf8', '#c084fc', '#f472b6', '#e879f9', '#60a5fa', '#a78bfa', '#fb7185'];
  const c = getThemeColors();

  chartInstance = new Chart(document.getElementById('spend-chart').getContext('2d'), {
    type: 'doughnut',
    data: {
      labels,
      datasets: [{
        data: values,
        backgroundColor: palette.slice(0, labels.length),
        borderColor: c.border,
        borderWidth: 3,
        hoverOffset: 10,
      }]
    },
    options: {
      responsive: true,
      maintainAspectRatio: false,
      cutout: '68%',
      plugins: {
        legend: {
          position: 'bottom',
          labels: {
            color: c.muted,
            font: {family: "'Nunito', sans-serif", weight: '700', size: 12},
            padding: 16, boxWidth: 10, boxHeight: 10,
          }
        },
        tooltip: {
          backgroundColor: c.tooltip,
          borderColor: c.tooltipBorder,
          borderWidth: 1,
          titleColor: c.text,
          bodyColor: c.muted,
          titleFont: {family: "'Nunito', sans-serif", weight: 'bold'},
          bodyFont: {family: "'Rubik', sans-serif", size: 13},
          callbacks: {
            label: ctx => ` ${new Intl.NumberFormat('en-US',
                {style: 'currency', currency: 'EUR'}).format(ctx.parsed)}`
          }
        }
      }
    }
  });
}

init();