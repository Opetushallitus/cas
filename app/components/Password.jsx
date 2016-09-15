import React from 'react';
import {translation} from '../resources/translations';

export default class Password extends React.Component {
  
  constructor(props){
    super()
    this.toggleMode = props.controller.modeChange
    this.state = {username: ''}
  }
  
  render() {
    const controller = this.props.controller;
    const successful = this.props.success;
    return(
      <div className="login-box">
        <div className="return-link" onClick={this.toggleMode}>
          {translation("login.returnLink")}
        </div>

          <p><strong>{translation("login.forgotPasswordTitle")}</strong></p>
        {successful ?
        <div className="password-reset-success">
          {translation("passwordRequestSuccessful")}
        </div> :
        <div className="password-reset-form">
          {translation("login.passwordRequestInfo")}

          <div>
            <input type="text" placeholder={translation("login.usernamePlaceholder")} onChange={e => this.setState({username: e.target.value})}/>
          </div>
          <div className="password-button-container">
            <button className="btn btn-login" onClick={() => controller.requestPassword(this.state.username)}>{translation("login.sendPasswordRequest")}</button>
          </div>
        </div>}
      </div>
    )
  }
}