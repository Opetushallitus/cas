import React from 'react';
import {translateNotification} from '../translations'
export default class Notices extends React.Component {

  render(){
    const notices = this.props.notices.map(n =>
      <div className="notice" key={n["id"]}>
        {translateNotification('text', n)}
       </div>);
    return(
      <div className="notices">{notices}</div>
    )
  }
}