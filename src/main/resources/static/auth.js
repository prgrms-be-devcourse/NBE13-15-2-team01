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

function showAccount(member) {
  openLogin.hidden = Boolean(member);
  account.hidden = !member;
  accountName.textContent = member?.name ?? '';
  accountName.title = accountName.textContent;
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
      throw new Error('로그인 응답에서 토큰과 회원 정보를 확인할 수 없습니다.');
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

logout.addEventListener('click', () => {
  clearAccessToken();
  showAccount(null);
  openLogin.focus();
});

if (new URLSearchParams(location.search).get('preview') !== '1') restoreAccount();
