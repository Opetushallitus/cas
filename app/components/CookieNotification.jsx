import React from 'react'
import {translation} from '../resources/translations';

export default class CookieNotification extends React.Component {

  constructor(props){
    super();
    this.acceptCookies = props.controller.acceptCookies;
  }

  render(){
    return(
        <div>
      <div className="cookieHeader">
        {translation("cookie.text")}
        <button className="btn btn-cookies" onClick={this.acceptCookies}>{translation("cookie.button")}</button>
      </div>
      </div> )
  }
}