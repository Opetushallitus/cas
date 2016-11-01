import React from 'react'
import {translation} from '../translations';

const CookieNotification = ({onAccept}) => {

  return(
      <div>
        <div className="cookieHeader">
          <span className="cookieText"> {translation("cookie.text")}</span>
          <span className="btn-cookies" onClick={onAccept}>{translation("cookie.button")}</span>
        </div>
    </div> )
};

export default CookieNotification;