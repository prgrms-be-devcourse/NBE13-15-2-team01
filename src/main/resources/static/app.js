import { requestApi } from './api.js';

const preview = new URLSearchParams(location.search).get('preview') === '1';
const byId = (id) => document.getElementById(id);
const search = byId('search');
const filters = byId('filters');
const count = byId('results-count');
const catalogStatus = byId('catalog-status');
const cards = byId('cards');
const dialog = byId('detail');
const detailSummary = byId('detail-summary');
const scheduleSection = byId('schedule-section');
const schedules = byId('schedules');
const seatSection = byId('seat-section');
const seats = byId('seats');

const covers = new Map([
  ['뮤지컬', 'images/musical.jpg'],
  ['연극', 'images/play.jpg'],
  ['콘서트', 'images/concert.jpg'],
]);
const numberFormat = new Intl.NumberFormat('ko-KR');
const dayFormat = new Intl.DateTimeFormat('ko-KR', {
  timeZone: 'Asia/Seoul', month: 'long', day: 'numeric', weekday: 'short',
});
const timeFormat = new Intl.DateTimeFormat('ko-KR', {
  timeZone: 'Asia/Seoul', hour: 'numeric', minute: '2-digit',
});

let performances = [];
let activeGenre;
let detailRequest;
let seatRequest;

function element(tag, className, content) {
  const node = document.createElement(tag);
  if (className) node.className = className;
  if (content !== undefined) node.textContent = content;
  return node;
}

function button(className, label, action) {
  const node = element('button', className, label);
  node.type = 'button';
  node.addEventListener('click', action);
  return node;
}

function showStatus(container, message, actions = []) {
  const box = element('div', 'status-box');
  const text = element('p', '', message);
  text.setAttribute('role', 'status');
  box.append(text);
  if (actions.length) {
    const controls = element('div', 'status-actions');
    controls.append(...actions);
    box.append(controls);
  }
  container.replaceChildren(box);
}

async function getData(path, signal) {
  if (preview) {
    const { getPreviewData } = await import('./preview.js');
    signal?.throwIfAborted();
    return getPreviewData(path);
  }
  return requestApi(path, { signal });
}

function genreOf(performance) {
  return performance.genre?.trim() || '기타';
}

function dateRange({ startDate, endDate }) {
  const format = (date) => date?.replaceAll('-', '.');
  if (startDate && endDate && startDate !== endDate) {
    return `${format(startDate)} – ${format(endDate)}`;
  }
  return format(startDate || endDate) || '공연 기간 미정';
}

function scheduleTime(value) {
  const date = new Date(`${value}+09:00`);
  if (Number.isNaN(date.getTime())) return '시간 미정';
  return `${dayFormat.format(date)} · ${timeFormat.format(date)}`;
}

function posterFor(performance, lazy = false) {
  const frame = element('span', 'poster');
  const img = element('img');
  const fallback = covers.get(genreOf(performance)) ?? 'images/theater.jpg';
  img.alt = '';
  img.decoding = 'async';
  if (lazy) img.loading = 'lazy';
  img.src = fallback;

  if (performance.posterUrl) {
    try {
      const url = new URL(performance.posterUrl, location.href);
      if (url.protocol === 'https:' || url.protocol === 'http:') {
        img.dataset.cover = 'poster';
        img.addEventListener('error', () => {
          delete img.dataset.cover;
          img.src = fallback;
        }, { once: true });
        img.src = url.href;
      }
    } catch {
      // Keep the genre cover if the stored poster URL is invalid.
    }
  }
  frame.append(img);
  return frame;
}

function makeCard(performance) {
  const card = button('card', undefined, () => openPerformance(performance));
  card.setAttribute('aria-label', `${performance.title} 상세 보기`);
  const info = element('span', 'card-info');
  const link = element('span', 'card-link', '회차·좌석 보기');
  link.append(element('span', '', '↗'));
  info.append(
    element('span', 'genre', genreOf(performance)),
    element('span', 'card-title', performance.title),
    element('span', 'card-meta', performance.venueName || '공연장 미정'),
    element('span', 'card-meta', dateRange(performance)),
    link,
  );
  card.append(posterFor(performance, true), info);
  return card;
}

function applyGenre(genre) {
  activeGenre = genre;
  for (const filter of filters.children) {
    filter.setAttribute('aria-pressed', String((filter.dataset.genre ?? undefined) === genre));
  }
  renderCards();
}

function renderFilters() {
  const genres = new Set(performances.map(genreOf));
  const allButton = button('filter', '전체', () => applyGenre(undefined));
  allButton.setAttribute('aria-pressed', 'true');
  filters.replaceChildren(allButton);
  for (const genre of genres) {
    const filter = button('filter', genre, () => applyGenre(genre));
    filter.dataset.genre = genre;
    filter.setAttribute('aria-pressed', 'false');
    filters.append(filter);
  }
}

function previewLink() {
  const link = element('a', 'button', '샘플 화면 보기');
  link.href = '?preview=1#performances';
  return link;
}

function renderCards() {
  const query = search.value.trim().toLocaleLowerCase('ko-KR');
  const matches = performances.filter((performance) => {
    const matchesGenre = !activeGenre || genreOf(performance) === activeGenre;
    const matchesQuery = [performance.title, performance.venueName]
      .some((value) => value?.toLocaleLowerCase('ko-KR').includes(query));
    return matchesGenre && matchesQuery;
  });
  count.textContent = `공연 ${numberFormat.format(matches.length)}개`;
  cards.replaceChildren(...matches.map(makeCard));
  catalogStatus.replaceChildren();
  if (!performances.length) {
    showStatus(catalogStatus, '등록된 공연이 없습니다.', [previewLink()]);
  } else if (!matches.length) {
    showStatus(catalogStatus, '검색 조건에 맞는 공연이 없습니다.', [
      button('button', '검색 초기화', () => {
        search.value = '';
        search.focus();
        applyGenre(undefined);
      }),
    ]);
  }
}

async function loadCatalog() {
  showStatus(catalogStatus, '공연을 불러오는 중입니다.');
  try {
    performances = await getData('performances');
    search.disabled = !performances.length;
    renderFilters();
    renderCards();
  } catch (error) {
    showStatus(catalogStatus, error.message || '공연 목록을 불러올 수 없습니다.', [
      button('button', '다시 시도', loadCatalog),
      previewLink(),
    ]);
  }
}

function renderSummary(performance) {
  const summary = element('div', 'detail-summary');
  const info = element('div');
  const meta = element('dl', 'detail-meta');
  for (const [label, value] of [
    ['공연장', performance.venueName || '미정'],
    ['공연 기간', dateRange(performance)],
  ]) {
    const row = element('div');
    row.append(element('dt', '', label), element('dd', '', value));
    meta.append(row);
  }
  info.append(
    element('span', 'genre', genreOf(performance)),
    element('h3', 'detail-title', performance.title),
    meta,
  );
  summary.append(posterFor(performance), info);
  detailSummary.replaceChildren(summary);
}

async function openPerformance(performance) {
  detailRequest?.abort();
  seatRequest?.abort();
  detailRequest = new AbortController();
  const { signal } = detailRequest;
  seatSection.hidden = true;
  seats.replaceChildren();
  scheduleSection.hidden = true;
  showStatus(detailSummary, '공연 상세 정보를 불러오는 중입니다.');
  if (!dialog.open) dialog.showModal();
  dialog.scrollTop = 0;

  try {
    const detail = await getData(`performances/${performance.id}`, signal);
    if (signal.aborted) return;
    renderSummary(detail);
    loadSchedules(detail.id, signal);
  } catch (error) {
    if (signal.aborted) return;
    showStatus(detailSummary, error.message || '공연 상세 정보를 불러올 수 없습니다.', [
      button('button', '다시 시도', () => openPerformance(performance)),
    ]);
  }
}

async function loadSchedules(performanceId, signal) {
  scheduleSection.hidden = false;
  showStatus(schedules, '회차를 불러오는 중입니다.');
  try {
    const list = await getData(`performances/${performanceId}/schedules`, signal);
    if (signal.aborted) return;
    if (!list.length) {
      showStatus(schedules, '등록된 회차가 없습니다.');
      return;
    }

    const options = element('div', 'schedule-list');
    for (const schedule of list) {
      const option = button('button schedule-button', scheduleTime(schedule.startAt), () => {
        for (const control of options.children) control.setAttribute('aria-pressed', String(control === option));
        loadSeats(schedule);
      });
      option.setAttribute('aria-pressed', 'false');
      option.append(element('small', '', schedule.status === 'OPEN' ? '좌석 조회 가능' : '마감'));
      options.append(option);
    }
    schedules.replaceChildren(options);
  } catch (error) {
    if (signal.aborted) return;
    showStatus(schedules, error.message || '회차를 불러올 수 없습니다.', [
      button('button', '다시 시도', () => loadSchedules(performanceId, signal)),
    ]);
  }
}

function renderSeats(list, schedule) {
  if (!list.length) {
    showStatus(seats, '등록된 좌석이 없습니다.');
    return;
  }
  const availableCount = list.filter((seat) => seat.available).length;
  const key = element('div', 'seat-key');
  key.append(
    element('span', '', '선택 가능'),
    element('span', 'key-selected', '선택됨'),
    element('span', 'key-unavailable', '선택 불가'),
  );
  const grid = element('div', 'seat-grid');
  const selected = new Map();
  const total = element('p', 'seat-total');
  const notice = element('p', 'seat-help seat-message');
  notice.setAttribute('role', 'status');
  total.setAttribute('role', 'status');

  function updateTotal() {
    const selectedSeats = [...selected.values()];
    const price = selectedSeats.reduce((sum, seat) => sum + seat.price, 0);
    const names = selectedSeats.map((seat) => seat.seatName).join(', ');
    total.textContent = names ? `${names} · ${selected.size}석 · ${numberFormat.format(price)}원` : '선택한 좌석 0석';
  }

  for (const seat of list) {
    const label = `${seat.seatName} · ${seat.grade}석 · ${numberFormat.format(seat.price)}원`;
    const option = button('seat', seat.seatName, () => {
      if (selected.has(seat.id)) {
        selected.delete(seat.id);
      } else if (selected.size < 3) {
        selected.set(seat.id, seat);
      } else {
        notice.textContent = '한 번에 최대 3석까지 선택할 수 있습니다.';
        return;
      }
      option.setAttribute('aria-pressed', String(selected.has(seat.id)));
      notice.textContent = '';
      updateTotal();
    });
    option.disabled = !seat.available;
    option.setAttribute('aria-label', label);
    option.setAttribute('aria-pressed', 'false');
    option.title = label;
    grid.append(option);
  }

  const availability = schedule.status === 'OPEN'
    ? `선택 가능 ${numberFormat.format(availableCount)}석 / 전체 ${numberFormat.format(list.length)}석`
    : '마감된 회차입니다.';
  seats.replaceChildren(element('p', 'seat-help', availability), key, grid);
  if (availableCount) {
    updateTotal();
    seats.append(total, element('p', 'seat-help', '좌석을 선택해 예상 금액을 확인할 수 있습니다. 예매 기능은 준비 중입니다.'), notice);
  }
}

async function loadSeats(schedule) {
  seatRequest?.abort();
  seatRequest = new AbortController();
  const { signal } = seatRequest;
  seatSection.hidden = false;
  showStatus(seats, '좌석을 불러오는 중입니다.');
  try {
    const list = await getData(`schedules/${schedule.id}/seats`, signal);
    if (!signal.aborted) renderSeats(list, schedule);
  } catch (error) {
    if (signal.aborted) return;
    showStatus(seats, error.message || '좌석을 불러올 수 없습니다.', [
      button('button', '다시 시도', () => loadSeats(schedule)),
    ]);
  }
}

dialog.addEventListener('close', () => {
  if (dialog.open) return;
  detailRequest?.abort();
  seatRequest?.abort();
});
search.addEventListener('input', renderCards);
byId('preview-banner').hidden = !preview;
loadCatalog();
