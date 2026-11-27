import { accessToken, clearAccessToken, requestApi, saveAccessToken } from './api.js';

const openLogin = document.getElementById('open-login');
const closeLogin = document.getElementById('close-login');
const dialog = document.getElementById('login-dialog');
const form = document.getElementById('login-form');
const submit = document.getElementById('submit-login');
const errorMessage = document.getElementById('login-error');
const account = document.getElementById('account');
const accountName = document.getElementById('account-name');
const logout = document.getElementById('logout');
const loginNotice = document.getElementById('login-notice');
const openSignup = document.getElementById('open-signup');
const closeSignup = document.getElementById('close-signup');
const signupDialog = document.getElementById('signup-dialog');
const signupForm = document.getElementById('signup-form');
const submitSignup = document.getElementById('submit-signup');
const signupError = document.getElementById('signup-error');
const openPerformanceForm = document.getElementById('open-performance-form');
const closePerformanceForm = document.getElementById('close-performance-form');
const performanceDialog = document.getElementById('performance-form-dialog');
const performanceForm = document.getElementById('performance-form');
const submitPerformance = document.getElementById('submit-performance');
const performanceError = document.getElementById('performance-form-error');
const openProfile = document.getElementById('open-profile');
const profileDialog = document.getElementById('profile-dialog');
const profileStatus = document.getElementById('profile-status');
const profileFields = document.getElementById('profile-fields');
const profileName = document.getElementById('profile-name');
const profileEmail = document.getElementById('profile-email');
const profileRole = document.getElementById('profile-role');

let profileRequest;
let currentMember;

export function isAdmin() {
  return currentMember?.role === 'ADMIN';
}

function showAccount(member) {
  currentMember = member;
  openLogin.hidden = Boolean(member);
  openSignup.hidden = Boolean(member);
  account.hidden = !member;
  openPerformanceForm.hidden = !isAdmin();
  accountName.textContent = member?.name ?? '';
  accountName.title = accountName.textContent;
  document.dispatchEvent(new Event('account-changed'));
}

async function restoreAccount() {
  if (accessToken()) {
    try {
      showAccount(await requestApi('members/me', { authenticated: true }));
      return;
    } catch {
      // Keep the login button available when the previous account can't be restored.
    }
  }
  showAccount(null);
}

openLogin.addEventListener('click', () => {
  errorMessage.textContent = '';
  loginNotice.textContent = '';
  dialog.showModal();
});

dialog.addEventListener('cancel', (event) => {
  if (submit.disabled) event.preventDefault();
});
dialog.addEventListener('close', () => form.reset());

form.addEventListener('submit', async (event) => {
  event.preventDefault();
  if (submit.disabled) return;
  const { email, password } = Object.fromEntries(new FormData(form));
  errorMessage.textContent = '';
  submit.disabled = closeLogin.disabled = true;
  try {
    const result = await requestApi('auth/login', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ email: email.trim(), password }),
    });
    if (typeof result?.accessToken !== 'string' || !result.accessToken.trim() || !result.member) {
      errorMessage.textContent = '로그인 응답에서 토큰과 회원 정보를 확인할 수 없습니다.';
      return;
    }
    saveAccessToken(result.accessToken);
    showAccount(result.member);
    dialog.close();
    logout.focus();
  } catch (error) {
    errorMessage.textContent = error.message || '로그인할 수 없습니다. 잠시 후 다시 시도해 주세요.';
  } finally {
    submit.disabled = closeLogin.disabled = false;
  }
});

openSignup.addEventListener('click', () => {
  signupError.textContent = '';
  signupDialog.showModal();
});

signupDialog.addEventListener('cancel', (event) => {
  if (submitSignup.disabled) event.preventDefault();
});
signupDialog.addEventListener('close', () => signupForm.reset());

signupForm.addEventListener('submit', async (event) => {
  event.preventDefault();
  if (submitSignup.disabled) return;
  const { name, email, password } = Object.fromEntries(new FormData(signupForm));
  signupError.textContent = '';
  submitSignup.disabled = closeSignup.disabled = true;
  try {
    const member = await requestApi('members', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ name: name.trim(), email: email.trim(), password }),
    });
    signupDialog.close();
    form.elements.namedItem('email').value = member.email;
    errorMessage.textContent = '';
    loginNotice.textContent = '가입이 완료됐습니다. 비밀번호를 입력해 로그인해 주세요.';
    dialog.showModal();
    form.elements.namedItem('password').focus();
  } catch (error) {
    signupError.textContent = error.message || '회원가입에 실패했습니다. 잠시 후 다시 시도해 주세요.';
  } finally {
    submitSignup.disabled = closeSignup.disabled = false;
  }
});

logout.addEventListener('click', () => {
  clearAccessToken();
  showAccount(null);
  openLogin.focus();
});

openProfile.addEventListener('click', async () => {
  profileRequest?.abort();
  profileRequest = new AbortController();
  const { signal } = profileRequest;
  profileStatus.textContent = '회원 정보를 불러오는 중입니다.';
  profileFields.hidden = true;
  profileDialog.showModal();
  try {
    const member = await requestApi('members/me', { authenticated: true, signal });
    if (signal.aborted) return;
    showAccount(member);
    profileName.textContent = member.name;
    profileEmail.textContent = member.email;
    profileRole.textContent = member.role === 'ADMIN' ? '관리자' : '일반 회원';
    profileStatus.textContent = '';
    profileFields.hidden = false;
  } catch (error) {
    if (signal.aborted) return;
    if (error.status === 401) showAccount(null);
    profileStatus.textContent = error.message || '회원 정보를 불러오지 못했습니다.';
  }
});

profileDialog.addEventListener('close', () => {
  if (!profileDialog.open) profileRequest?.abort();
});

openPerformanceForm.addEventListener('click', () => {
  performanceError.textContent = '';
  performanceDialog.showModal();
});

performanceDialog.addEventListener('cancel', (event) => {
  if (submitPerformance.disabled) event.preventDefault();
});
performanceDialog.addEventListener('close', () => performanceForm.reset());

performanceForm.addEventListener('submit', async (event) => {
  event.preventDefault();
  if (submitPerformance.disabled) return;

  const values = Array.from(new FormData(performanceForm), ([key, value]) => [key, value.trim()]);
  const fields = Object.fromEntries(values.filter(([, value]) => value));
  if (!fields.title) {
    performanceError.textContent = '공연명을 입력해 주세요.';
    return;
  }
  if (fields.startDate && fields.endDate && fields.endDate < fields.startDate) {
    performanceError.textContent = '종료일은 시작일과 같거나 이후여야 합니다.';
    return;
  }
  if (fields.posterUrl && !/^https?:\/\//i.test(fields.posterUrl)) {
    performanceError.textContent = '포스터 URL은 http 또는 https 주소로 입력해 주세요.';
    return;
  }

  performanceError.textContent = '';
  submitPerformance.disabled = closePerformanceForm.disabled = true;
  try {
    const performance = await requestApi('performances', {
      authenticated: true,
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(fields),
    });
    performanceDialog.close();
    document.dispatchEvent(new CustomEvent('performance-registered', { detail: performance }));
  } catch (error) {
    if (error.status === 401) showAccount(null);
    performanceError.textContent = error.status === 403
      ? '관리자 권한이 필요합니다. 권한이 변경됐다면 다시 로그인해 주세요.'
      : error.message || '공연을 등록하지 못했습니다. 잠시 후 다시 시도해 주세요.';
  } finally {
    submitPerformance.disabled = closePerformanceForm.disabled = false;
  }
});

if (new URLSearchParams(location.search).get('preview') !== '1') restoreAccount();
