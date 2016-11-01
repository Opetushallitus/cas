import React from 'react';
import {translation} from '../translations';

function resetNotification(resetDone){
  return(
    resetDone.success ?
      <div className="password-reset-success">
        {translation("login.passwordRequestSuccessful")}
      </div> :
      <div className="password-reset-failure">
        {translation("login.passwordRequestFailed")}
      </div>)
}

const Password = ({modeChange, requestPassword, onChange, resetStatus, username}) => {
  
  const _requestPassword = (e, username) => {
    e.preventDefault();
    requestPassword(username)
  };

  const resetDone = resetStatus.reset;

  return(
    <div className={resetDone ? "short-box" : "login-box"}>
      <div className="return-link" onClick={modeChange}>
        {translation("login.returnLink")}
      </div>
      <p><strong>{translation("login.forgotPasswordTitle")}</strong></p>
      {resetDone ?
        resetNotification(resetStatus) :
      <div className="password-reset-form">
        {translation("login.passwordRequestInfo")}
        <form className="resetPasswordForm" onSubmit={_requestPassword}>
          <div>
            <input className="login-input" value={username} autoFocus="autoFocus" type="text" placeholder={translation("login.usernamePlaceholder")} onChange={e => onChange(e.target.value)}/>
          </div>
          <div className="password-button-container">
            <button className="btn btn-login" onClick={_requestPassword}>{translation("login.sendPasswordRequest")}</button>
          </div>
         </form>
      </div>}
    </div>
    )
}

export default Password
