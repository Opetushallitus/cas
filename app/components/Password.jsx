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
    return(
        <div className="login-box">
        <div className="link" onClick={this.toggleMode}>
          {translation("login.returnLink")}
        </div>
        <div>
          {translation("login.passwordRequestInfo")}
        </div>
        <div>
          <input type="text" placeholder={translation("login.usernamePlaceholder")} onChange={e => this.setState({username: e.target.value})}/>
        </div>
        <div>
          <button className="btn btn-login" onClick={() => controller.requestPassword(this.state.username)}>{translation("login.sendPasswordRequest")}</button>
        </div>
      </div>
    )
  }
}