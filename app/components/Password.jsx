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

export default class Password extends React.Component {
  
  constructor(props){
    super();
    this.controller = props.controller;
    this.toggleMode = props.controller.modeChange;
    this.state = {username: ''}
  }

  requestPassword(e, username){
    e.preventDefault();
    this.controller.requestPassword(username)
  }

  render() {
    const resetStatus = this.props.resetStatus;
    const resetDone = resetStatus.reset;

    return(
      <div className={resetDone ? "short-box" : "login-box"}>
        <div className="return-link" onClick={this.toggleMode}>
          {translation("login.returnLink")}
        </div>
        <p><strong>{translation("login.forgotPasswordTitle")}</strong></p>
        {resetDone ?
          resetNotification(this.props.resetStatus) :
        <div className="password-reset-form">
          {translation("login.passwordRequestInfo")}
          <form className="resetPasswordForm" onSubmit={(e) => this.requestPassword(e, this.state.username)}>
            <div>
              <input className="login-input" autoFocus="autoFocus" type="text" placeholder={translation("login.usernamePlaceholder")} onChange={e => this.setState({username: e.target.value})}/>
            </div>
            <div className="password-button-container">
              <button className="btn btn-login" onClick={() => controller.requestPassword(this.state.username)}>{translation("login.sendPasswordRequest")}</button>
            </div>
           </form>
        </div>}
      </div>
    )
  }
}