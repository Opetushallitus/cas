import React from 'react'
import {translation} from '../translations';

export default class CookieNotification extends React.Component {

  constructor(props){
    super();
    this.acceptCookies = props.controller.acceptCookies;
  }

  render(){
    return(
        <div>
          <div className="cookieHeader">
            <span className="cookieText"> {translation("cookie.text")}</span>
            <span className="btn-cookies" onClick={this.acceptCookies}>{translation("cookie.button")}</span>
          </div>
      </div> )
  }
}