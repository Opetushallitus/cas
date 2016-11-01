import React from 'react';
import {translateNotification} from '../translations'

const Notices = ({notices}) => {

  const _notices = notices.map(n =>
    <div className="notice" key={n["id"]}>
      {translateNotification('text', n)}
     </div>);

  return( <div className="notices">{_notices}</div>)
};

export default Notices;
