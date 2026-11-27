const performances = [
  {
    id: 1, title: '별빛 아래서', genre: '뮤지컬', venueName: '샘플 아트홀',
    startDate: '2026-12-10', endDate: '2026-12-31',
  },
  {
    id: 2, title: '기억의 방', genre: '연극', venueName: '샘플 소극장',
    startDate: '2026-12-12', endDate: '2026-12-28',
  },
  {
    id: 3, title: '겨울 밤 라이브', genre: '콘서트', venueName: '샘플 공연장',
    startDate: '2026-12-20', endDate: '2026-12-25',
  },
];

const schedules = [
  { id: 1, performanceId: 1, startAt: '2026-12-10T19:00:00', status: 'OPEN' },
  { id: 2, performanceId: 1, startAt: '2026-12-11T14:00:00', status: 'OPEN' },
  { id: 3, performanceId: 2, startAt: '2026-12-12T19:30:00', status: 'OPEN' },
  { id: 4, performanceId: 2, startAt: '2026-12-13T15:00:00', status: 'OPEN' },
  { id: 5, performanceId: 3, startAt: '2026-12-20T18:00:00', status: 'OPEN' },
  { id: 6, performanceId: 3, startAt: '2026-12-21T18:00:00', status: 'CLOSED' },
];

function previewSeats(schedule) {
  const rows = [
    ['A', 'VIP', 150000], ['B', 'VIP', 150000],
    ['C', 'R', 110000], ['D', 'R', 110000], ['E', 'S', 80000],
  ];
  return rows.flatMap(([row, grade, price], index) => Array.from({ length: 10 }, (_, column) => {
    const id = index * 10 + column + 1;
    return {
      id,
      seatName: `${row}${column + 1}`,
      grade,
      price,
      available: schedule.status === 'OPEN' && (id + schedule.id) % 6 !== 0,
    };
  }));
}

export function getPreviewData(path) {
  const [resource, rawId, child] = path.split('/');
  const id = Number(rawId);
  if (resource === 'performances') {
    if (!rawId) return performances;
    const performance = performances.find((entry) => entry.id === id);
    if (performance && child === 'schedules') return schedules.filter((schedule) => schedule.performanceId === id);
    if (performance) return performance;
  }
  if (resource === 'schedules' && child === 'seats') {
    const schedule = schedules.find((entry) => entry.id === id);
    if (schedule) return previewSeats(schedule);
  }
  throw new Error('샘플 데이터를 찾을 수 없습니다.');
}
