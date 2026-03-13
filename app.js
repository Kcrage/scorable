// ── State ────────────────────────────────────────
let state = {};

function freshState(t1, t2, totalOvers, wideRunCounts, noballRunCounts) {
  return {
    teams: [t1, t2],
    totalOvers,
    // scoring rules
    wideRunCounts,    // true = +1 run for wide, false = no run
    noballRunCounts,  // true = +1 run for no ball, false = no run
    innings: 0,
    runs:        [0, 0],
    wickets:     [0, 0],
    legalBalls:  [0, 0],
    extras:      [0, 0],
    history:     [[], []],
    currentOver: [[], []],
    target:      null,
    matchOver:   false,
  };
}

/**
 * Build a ball object.
 * runsAwarded respects the wide/no-ball scoring rules stored in state.
 */
function ballObj(type, baseRuns) {
  const legal = !(type === 'wide' || type === 'noball');

  let runsAwarded = baseRuns;
  if (type === 'wide'   && !state.wideRunCounts)   runsAwarded = 0;
  if (type === 'noball' && !state.noballRunCounts)  runsAwarded = 0;

  return { type, runs: runsAwarded, legal };
}

// ── Shared reset helper ───────────────────────────
function goToSetup() {
  document.getElementById('game-screen').style.display   = 'none';
  document.getElementById('result-banner').style.display = 'none';
  document.getElementById('setup-screen').style.display  = 'block';
}

// ── Live toggle label highlight on setup screen ──
function initToggleLabels() {
  ['wide-run-toggle', 'noball-run-toggle'].forEach(id => {
    const checkbox = document.getElementById(id);
    const row      = checkbox.closest('.rule-row');

    function sync() {
      row.classList.toggle('rule-active',   checkbox.checked);
      row.classList.toggle('rule-inactive', !checkbox.checked);
    }

    sync(); // initial state
    checkbox.addEventListener('change', sync);
  });
}

document.addEventListener('DOMContentLoaded', initToggleLabels);

// ── Setup ────────────────────────────────────────
document.getElementById('start-btn').addEventListener('click', () => {
  const t1              = document.getElementById('team1-input').value.trim() || 'Team A';
  const t2              = document.getElementById('team2-input').value.trim() || 'Team B';
  const ov              = parseInt(document.getElementById('overs-input').value) || 20;
  const wideRunCounts   = document.getElementById('wide-run-toggle').checked;
  const noballRunCounts = document.getElementById('noball-run-toggle').checked;

  state = freshState(t1, t2, ov, wideRunCounts, noballRunCounts);

  document.getElementById('setup-screen').style.display = 'none';
  document.getElementById('game-screen').style.display  = 'block';
  renderAll();
});

document.getElementById('new-match-btn').addEventListener('click', goToSetup);

document.getElementById('new-match-bottom-btn').addEventListener('click', () => {
  const confirmed = state.matchOver || confirm('Start a new match? The current match will be lost.');
  if (confirmed) goToSetup();
});

document.getElementById('start-2nd-btn').addEventListener('click', () => {
  document.getElementById('innings-modal').classList.remove('show');
  renderAll();
});

// ── Core Logic ───────────────────────────────────
function addBall(type, baseRuns) {
  if (state.matchOver) return;
  const inn  = state.innings;
  const ball = ballObj(type, baseRuns);

  state.currentOver[inn].push(ball);
  state.runs[inn]   += ball.runs;

  // extras counter: record the delivery as an extra regardless of run rule
  if (!ball.legal) state.extras[inn]++;

  if (ball.legal)        state.legalBalls[inn]++;
  if (type === 'wicket') state.wickets[inn]++;

  if (ball.legal && state.legalBalls[inn] % 6 === 0) {
    state.history[inn].push([...state.currentOver[inn]]);
    state.currentOver[inn] = [];
  }

  checkEndConditions();
  renderAll();
}

function undoLast() {
  if (state.matchOver) return;
  const inn = state.innings;

  if (state.currentOver[inn].length === 0) {
    if (state.history[inn].length === 0) return;
    state.currentOver[inn] = state.history[inn].pop();
  }

  const ball = state.currentOver[inn].pop();
  state.runs[inn]   -= ball.runs;
  if (!ball.legal)            state.extras[inn]--;
  if (ball.legal)             state.legalBalls[inn]--;
  if (ball.type === 'wicket') state.wickets[inn]--;

  renderAll();
}

// ── End-Condition Checks ─────────────────────────
function checkEndConditions() {
  const inn        = state.innings;
  const totalOvers = state.totalOvers;

  if (inn === 0) {
    const allOut    = state.wickets[0] >= 10;
    const oversDone = state.legalBalls[0] >= totalOvers * 6;
    if (allOut || oversDone) {
      state.target  = state.runs[0] + 1;
      state.innings = 1;
      showInningsBreak();
    }
  } else {
    const inn2Runs   = state.runs[1];
    const inn2Wkts   = state.wickets[1];
    const inn2Balls  = state.legalBalls[1];
    const ballsLimit = totalOvers * 6;

    if (inn2Runs >= state.target) {
      const wktsLeft = 10 - inn2Wkts;
      showResult(`${state.teams[1]} won by ${wktsLeft} wicket${wktsLeft !== 1 ? 's' : ''}! 🎉`, false);
    } else if (inn2Wkts >= 10 || inn2Balls >= ballsLimit) {
      const diff = state.target - 1 - inn2Runs;
      if (diff === 0) {
        showResult('Match Tied! 🤝', true);
      } else {
        showResult(`${state.teams[0]} won by ${diff} run${diff !== 1 ? 's' : ''}! 🏆`, false);
      }
    }
  }
}

// ── Modals & Banners ─────────────────────────────
function showInningsBreak() {
  const summary    = `${state.teams[0]} scored ${state.runs[0]}/${state.wickets[0]} in ${oversDisplay(0)}.`;
  const ballsAvail = state.totalOvers * 6;
  const rrr        = ((state.target / ballsAvail) * 6).toFixed(2);

  document.getElementById('modal-innings-summary').textContent = summary;
  document.getElementById('modal-target').textContent          = `Target: ${state.target}`;
  document.getElementById('modal-rrr-needed').textContent      =
    `${state.teams[1]} need ${state.target} runs in ${state.totalOvers} overs (RRR: ${rrr})`;

  document.getElementById('innings-modal').classList.add('show');
}

function showResult(msg, tie) {
  state.matchOver = true;
  const banner = document.getElementById('result-banner');
  banner.style.display = 'block';
  banner.className     = tie ? 'tie-result' : '';
  document.getElementById('result-text').textContent = msg;
}

// ── Helpers ──────────────────────────────────────
function oversDisplay(inn) {
  const full = Math.floor(state.legalBalls[inn] / 6);
  const rem  = state.legalBalls[inn] % 6;
  return `${full}.${rem}`;
}

function calcRRR() {
  if (state.innings !== 1 || !state.target) return null;
  const runsNeeded = state.target - state.runs[1];
  const ballsLeft  = state.totalOvers * 6 - state.legalBalls[1];
  if (ballsLeft <= 0 || runsNeeded <= 0) return null;

  const rrr    = (runsNeeded / ballsLeft) * 6;
  const rrrStr = rrr.toFixed(2);

  const oversLeft = ballsLeft;

  let difficulty, label, barPct;
  if (rrr <= 7)       { difficulty = 'easy';   label = 'Comfortable';    barPct = 20; }
  else if (rrr <= 10) { difficulty = 'medium'; label = 'Manageable';     barPct = 45; }
  else if (rrr <= 14) { difficulty = 'hard';   label = 'Challenging';    barPct = 72; }
  else                { difficulty = 'danger'; label = 'Near Impossible'; barPct = 95; }

  return { rrr, rrrStr, runsNeeded, ballsLeft, oversLeft, difficulty, label, barPct };
}
function chipClass(ball) { return ball.type; }

function chipLabel(ball) {
  switch (ball.type) {
    case 'dot':    return '0';
    case 'wicket': return 'W';
    case 'wide':   return 'Wd';
    case 'noball': return 'Nb';
    default:       return ball.runs;
  }
}

// ── Render ───────────────────────────────────────
function renderAll() {
  const inn = state.innings;

  document.getElementById('innings-label').textContent     = inn === 0 ? '1st Innings' : '2nd Innings';
  document.getElementById('batting-team-name').textContent = state.teams[inn];
  document.getElementById('score-display').textContent     = `${state.runs[inn]}/${state.wickets[inn]}`;
  document.getElementById('overs-display').textContent     = `Overs: ${oversDisplay(inn)} / ${state.totalOvers}`;
  document.getElementById('extras-row').textContent        = `Extras: ${state.extras[inn]}`;

  document.getElementById('rrr-badge').style.display = 'none';

  renderRulesBadges();
  renderRRRPanel();
  renderCurrentOver(inn);
  renderHistory(inn);
}

// ── Rule badges in scoreboard ────────────────────
function renderRulesBadges() {
  const container = document.getElementById('rules-badges');
  container.innerHTML = '';

  const badges = [
    { label: 'Wd+1', on: state.wideRunCounts },
    { label: 'Nb+1', on: state.noballRunCounts },
  ];

  badges.forEach(b => {
    const span       = document.createElement('span');
    span.className   = `rule-badge ${b.on ? 'on' : 'off'}`;
    span.textContent = b.label;
    container.appendChild(span);
  });
}

// ── RRR Panel ────────────────────────────────────
function renderRRRPanel() {
  const panel = document.getElementById('rrr-panel');
  const data  = calcRRR();

  if (!data) { panel.style.display = 'none'; return; }

  panel.style.display = 'block';
  panel.className     = `rrr-panel ${data.difficulty}`;

  document.getElementById('rrr-big').textContent        = data.rrrStr;
  document.getElementById('rrr-need').textContent       = data.runsNeeded;
  document.getElementById('rrr-balls-left').textContent = data.oversLeft;
document.getElementById('rrr-overs-left').textContent = 'balls left';
  document.getElementById('rrr-bar-fill').style.width   = `${data.barPct}%`;
  document.getElementById('rrr-bar-label').textContent  = data.label;
}

// ── Current Over chips ───────────────────────────
function renderCurrentOver(inn) {
  const balls = state.currentOver[inn].filter(b => b.legal);
  for (let i = 0; i < 6; i++) {
    const slot = document.getElementById(`slot-${i}`);
    if (i < balls.length) {
      const b          = balls[i];
      slot.className   = `ball-chip ${chipClass(b)}`;
      slot.textContent = chipLabel(b);
    } else {
      slot.className   = 'ball-chip';
      slot.textContent = '·';
    }
  }
}

// ── Ball history ─────────────────────────────────
function renderHistory(inn) {
  const container = document.getElementById('history-inner');
  container.innerHTML = '';

  const allOvers = [
    ...state.history[inn],
    ...(state.currentOver[inn].length > 0 ? [state.currentOver[inn]] : []),
  ];

  if (allOvers.length === 0) {
    container.innerHTML = '<span style="color:#334155;font-size:0.75rem;">No balls bowled yet</span>';
    return;
  }

  allOvers.forEach((over, idx) => {
    const label         = document.createElement('span');
    label.style.cssText = 'font-size:0.65rem;color:#475569;align-self:center;margin-right:2px;flex-shrink:0;';
    label.textContent   = `${idx + 1}:`;
    container.appendChild(label);

    over.forEach(ball => {
      const pill       = document.createElement('span');
      pill.className   = `h-pill ${chipClass(ball)}`;
      pill.textContent = chipLabel(ball);
      container.appendChild(pill);
    });

    if (idx < allOvers.length - 1) {
      const sep     = document.createElement('div');
      sep.className = 'h-sep';
      container.appendChild(sep);
    }
  });

  const box    = document.querySelector('.history-box');
  box.scrollLeft = box.scrollWidth;
}
