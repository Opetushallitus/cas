import React from 'react';
import {translation} from '../resources/translations';

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
    this.toggleMode = props.controller.modeChange;
    this.state = {username: ''}
  }

  render() {
    const controller = this.props.controller;
    const resetDone = this.props.resetDone.reset;
    return(
      <div className="login-box">
        <div className="return-link" onClick={this.toggleMode}>
          {translation("login.returnLink")}
        </div>
        <p><strong>{translation("login.forgotPasswordTitle")}</strong></p>
        {resetDone ?
          resetNotification(this.props.resetDone) :
        <div className="password-reset-form">
          {translation("login.passwordRequestInfo")}

          <div>
            <input autoFocus="autoFocus" type="text" placeholder={translation("login.usernamePlaceholder")} onChange={e => this.setState({username: e.target.value})}/>
          </div>
          <div className="password-button-container">
            <button className="btn btn-login" onClick={() => controller.requestPassword(this.state.username)}>{translation("login.sendPasswordRequest")}</button>
          </div>
        </div>}
      </div>
    )
  }
}